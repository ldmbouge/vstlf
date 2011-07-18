/************************************************************************
 MIT License

 Copyright (c) 2010 University of Connecticut

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***********************************************************************/

package edu.uconn.vstlf.batch;

import java.io.File;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.*;
import edu.uconn.vstlf.neuro.*;
import edu.uconn.vstlf.prediction.ANNSpec;
import edu.uconn.vstlf.prediction.LoadSeries;
import edu.uconn.vstlf.prediction.PredictionEngine;
import edu.uconn.vstlf.config.ANNConfig;
import edu.uconn.vstlf.config.Items;

import com.web_tomorrow.utils.suntimes.*;

public class VSTLFTrainer {		
	
	//////////////////////////////////////////////////////////
	//Number of decomposition levels  0 to 4/////////////////
	static int _lvls = 2;
	//////////////////////////////////////////////////////////
	
	static NormalizingFunction[] _norm, _denorm;
	static NormalizingFunction _normAbs, _denormAbs;
	//static SimilarDaySelector _selector;
	static Calendar _gmt = new Calendar("GMT");
	
	static int _lo, _up;
	
	static void setBounds(int lo, int up){
		_lo = lo;
		_up = up;
	}
		
	public static double[][] test (String loadFile, Date stTest, Date edTest, int lo, int up)
	{
		try{
			String methodName = "test";

			double[][] result = new double[12][];
			int offs = lo;
//			for(int offs=lo;offs<=up;offs++){
				MessageCenter.getInstance().put(
						new LogMessage(Level.INFO, VSTLFTrainer.class.getName(), methodName, 
								"Testing system for offset: " + 5*offs + " minutes."));
				///////////////////////////////////////////////////////////////////////////////////
				//Initialize db and cal et cetera
				///////////////////////////////////////////////////////////////////////////////////
				Calendar cal = new Calendar();
				PowerDB loadHist = new PerstPowerDB(loadFile,300);
				loadHist.open();
				///////////////////////////////////////////////////////////////////////////////////
				//Define test parameters//
				//////////////////////////////////////////////////////////////////////////////////
				Date ts = stTest;    				//st train
				Date ed = edTest;				//ed forecast
				ts = cal.addMinutesTo(ts, 5*offs);
				ed = cal.addMinutesTo(ed, 5*offs);
				NormalizingFunction[] normalizers = {new NormalizingFunction(-500,500,0,1), //h
													 new NormalizingFunction(-500,500,0,1),	//lh
													 new NormalizingFunction(-500,500,0,1),//llh
													 new NormalizingFunction(-500,500,0,1),//lllh
													 new NormalizingFunction(-.1,.1,0,1)};//llll
				NormalizingFunction[] denormalizers = {new NormalizingFunction(0,1,-500,500),
													   new NormalizingFunction(0,1,-500,500),//ditto
													   new NormalizingFunction(0,1,-500,500),
													   new NormalizingFunction(0,1,-500,500),
													   new NormalizingFunction(0,1,-.1,.1)};									  
				int[][] layerSizes = {{12+15+12+24+7,6,12},
									  {12+15+12+24+7,13,12},
									  {12+15+12+24+7,12,12},
									  {12+15+12+24+7,13,12},
									  {12+15+12+24+7,18,12}};
				_norm = new NormalizingFunction[_lvls+1];
				_denorm = new NormalizingFunction[_lvls+1];
				int[][] lyrSz = new int[_lvls+1][];
				for(int i = 0;i<_lvls;i++){
					_norm[i] = normalizers[i];
					_denorm[i] = denormalizers[i];
					lyrSz[i] = layerSizes[i];
				}
				_norm[_lvls] = normalizers[4];
				_denorm[_lvls] = denormalizers[4];
				double minload = new Double(Items.MinLoad.value());
				double maxload = new Double(Items.MaxLoad.value());
				_normAbs = new NormalizingFunction(minload,maxload,0,1);
				_denormAbs = new NormalizingFunction(0,1,minload,maxload);
				lyrSz[_lvls] = layerSizes[4];
				///////////////////////////////////////////////////////////////////////////////
				//Load ANNs
				///////////////////////////////////////////////////////////////////////////////
				ANNBank anns;
				try{
					anns= new ANNBank("./anns/bank"+offs+".ann");
				}
				catch(Exception e){
					throw new Exception("No ANNs have been trained");
				}
				///////////////////////////////////////////////////////////////////////////////
				//Run forecast test
				//////////////////////////////////////////////////////////////////////////////
				
				Vector<Series> errV = new Vector<Series>();
				Vector<Series> difV = new Vector<Series>();
				MessageCenter.getInstance().put(
						new LogMessage(Level.INFO, VSTLFTrainer.class.getName(), methodName,
								"Running forecast test.\nForecasting from "+cal.string(ts)+" to "+ cal.string(ed)));
				while(ts.before(ed)){
					Series[] out = anns.execute(inputSetFor(ts,cal,loadHist));
					anns.update(targetSetFor(ts, cal, loadHist));
					Series pred = new Series(12);
					for(int i = 0;i<_lvls;i++){
						out[i] = _denorm[i].imageOf(out[i]);
						pred = pred.plus(out[i]);
					}
					out[_lvls] = _denorm[_lvls].imageOf(out[_lvls]);
					Series prev = loadHist.getLoad("filt", cal.addHoursTo(ts, -11), cal.addHoursTo(ts, 0))
										  .daub4Separation(_lvls, _db4LD,_db4HD,_db4LR,_db4HR)[_lvls];
					out[_lvls] = out[_lvls].undifferentiate(prev.suffix(1).element(1));
					pred = pred.plus(out[_lvls]);
					Series act = loadHist.getLoad("filt", cal.addHoursTo(ts, -10), cal.addHoursTo(ts, 1));
					
					
					//Separate the actual load.////////////
					Series[] real = act.daub4Separation(_lvls, _db4LD,_db4HD,_db4LR,_db4HR);
					Series sumAct = new Series(12);
					for(int i = 0;i<=_lvls;i++){
						real[i] = real[i].subseries(121, 132);
						sumAct = sumAct.plus(real[i]);
					}
					act = act.subseries(121,132);
					Series err = new ErrorFunction().imageOf(act,pred);
					Series dif = act.minus(pred);
					//if(cal.getHour(ts) == _hr){
						errV.add(err);
						difV.add(dif);
					//}
					//System.out.println(act.element(12)+"\t"+pred.element(12));
					
					//System.out.println(ts+":");
					//System.out.println(act);
					//System.out.println(pred);
					//System.out.println(err);
					//System.out.println();
					ts = cal.addMinutesTo(ts, 60);
					//ts = cal.addDaysTo(ts, 1);
					
				}
				
				////////////////////////////////////////////////////////////////////////////////////////
				//Compute accuracy statistics.
				///////////////////////////////////////////////////////////////////////////////////////
				Series[] errs = new Series[errV.size()];
				Series[] difs = new Series[difV.size()];
				for(int k = 0;k<errs.length;k++){
					errs[k] = errV.elementAt(k);
					difs[k] = difV.elementAt(k);
				}
				int k = errs.length;
				Series aveErr = new MeanFunction().imageOf(errs);
				Series stdErr = new StDevFunction().imageOf(errs);
				Series aveDif = new MeanFunction().imageOf(difs);
				Series stdDif = new StDevFunction().imageOf(difs);
				MessageCenter.getInstance().put(
						new LogMessage(Level.INFO, VSTLFTrainer.class.getName(),
								methodName, k + " forecasts made.\nMean Err: \t" + aveErr + "\nstd(Err): \t" + stdErr + "\nMean Dif: \t" + aveDif + "\nstd(Dif): \t" + stdDif));	
				//////////////////////////////////////////////////////////////////////////////////////
				//Finish up the iteration.
				//////////////////////////////////////////////////////////////////////////////////////
				//wdb.close();
				loadHist.close();
				result[offs] = aveErr.array();
				return result;
			//}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	//Training////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	public static void train(String loadFile, Date stTrain, Date edTrain, int lo, int up){
		train(loadFile, stTrain, edTrain, 1*300,2*300,3*300,6*300,24*300,.0,.0,.0,.0,.0,lo,up);
	}
	
	public static void train(String loadFile, Date stTrain, Date edTrain,
								   int th, int tlh, int tllh, int tlllh, int tllll,
								   double eh, double elh, double ellh, double elllh, double ellll, int lo, int up){
		try{
			String methodName = "train";
			
			MessageCenter.getInstance().put(
					new LogMessage(Level.INFO, VSTLFTrainer.class.getName(), methodName, 
							"Training Neural Networks " + lo + " to " + up));

			Calendar cal = new Calendar();
			
			///////////////////////////////////////////////////////////////////////////////////
			//Initialize db and cal et cetera
			///////////////////////////////////////////////////////////////////////////////////
			PowerDB loadHist = new PerstPowerDB(loadFile,300);
			loadHist.open();

			////////////////////////////////////////////////////////////////////////////////
			//Build training set
			/////////////////////////////////////////////////////////////////////////////////
			Series load = loadHist.getLoad("filt", stTrain, edTrain);
			CheckLoadIntegrity("Target data set", load, stTrain);
			Series beforePatchLoad = load;
			load = patchSpikesLargerThan(load, 500, 2, 10);
			LogSpikes(beforePatchLoad, load, stTrain, edTrain, 500);
			LoadSeries loadSeries = new LoadSeries(load, edTrain, cal, 300);
			
			/////////////////////////////////////
			//// Create the pred engine
			/////////////////////////////////////
			ANNSpec[] annSpecs = ANNConfig.getInstance().getANNSpecs();
			int nANNInBank = annSpecs.length;
			int[][] lyrSz = new int[nANNInBank][];
			for (int i = 0; i < nANNInBank; ++i)
				lyrSz[i] = annSpecs[i].getLayerSize();

			ANNBank[] annBanks = new ANNBank[12];
			PredictionEngine predEngine = new PredictionEngine(annBanks);
			
			///////////////////////////////////////////////////////////////////////////////
			//Make ANNs
			///////////////////////////////////////////////////////////////////////////////
			for(int offs=0;offs<=11;offs++){
				ANNBank anns;
				try{
					File f = new File("bank"+offs+".ann");
					if (f.exists())
						anns= new ANNBank("bank"+offs+".ann");
					else
						anns = new ANNBank(lyrSz);
				}
				catch(Exception e){
					throw e;
				}
				annBanks[offs] = anns;
			}

			
			/////////////////////////////////
			int[] times = {th,tlh,tllh,tlllh,tllll};
			double[] errors = {eh,elh,ellh,elllh,ellll};
			int[] sec = new int[nANNInBank];
			double[] mse = new double[nANNInBank];
			for(int i = 0;i<nANNInBank-1;i++){
				sec[i] = times[i];
				mse[i] = errors[i];
			}
			sec[nANNInBank-1] = times[4];
			mse[nANNInBank-1] = errors[4];
			
			//////////////////////////////////////////////
			/// Train the network
			/////////////////////////////////////////////
			for(int offs=lo;offs<=up;offs++){
				ANNBank anns = annBanks[offs];
				
				for (int i = 0; i < nANNInBank; ++i) {
					// disable all the networks except the network i at bank offs
					for (int bid = 0; bid < 12; ++bid)
						for (int annId = 0; annId < nANNInBank; ++annId)
							annBanks[bid].disableNetwork(annId);
					anns.enableNetwork(i);
					
					trainWithPredEngine(loadSeries, predEngine, sec[i]);
				}
				
				
				annBanks[offs].save("bank"+offs+".ann");
			}
			
			loadHist.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void trainWithPredEngine(LoadSeries trainLoad, PredictionEngine pred, int seconds) throws Exception
	{
		long st = System.currentTimeMillis();
		long dt = seconds*1000;
		
		Calendar cal = trainLoad.getCal();
		// set the when the training should start from
		Date stTrain = cal.addHoursTo(trainLoad.getStartTime(), 15);
		Date edTrain = trainLoad.getCurTime();
		Date trainTime = stTrain;
		do{
			LoadSeries ls = trainLoad.getSubSeries(cal.addHoursTo(trainTime, -15), trainTime);
			pred.update(ls);
			trainTime = cal.addSecondsTo(trainTime, 300);
			if (trainTime.after(edTrain)) trainTime = stTrain;
		} while(System.currentTimeMillis() < st+dt);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, "VSTLFTrainer", "trainWithPredEngine", "\tDone."));

		MessageCenter.getInstance().put(
				new LogMessage(Level.INFO, VSTLFTrainer.class.getName(),
						"trainWithPredEngine", "after "+ (System.currentTimeMillis()-st)));
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//Define Input Vector Set
	////////////////////////////////////////////////////////////////////////////////////////////
	public static Series[] inputSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		///Time Index
		double[] wdi = new double[7];     // week-day index
		double[] hri = new double[24];    // hour index 
		double[] mid = new double[12];    // 5 minute block index
		hri[cal.getHour(t)] = 1;
		wdi[cal.getDayOfWeek(t)-1] = 1;
		mid[cal.getMonth(t)] = 1;
		// The index arrays are full of 0 except for the entries identifying the start time t
		
		
		double[] sunHr = new double[3];
		double[] sunMin = new double[12];
		int zYr = _gmt.getYear(t);
		int zMonth = _gmt.getMonth(t)+1;
		int zDay = _gmt.getDate(t);
		double longitude = new Double(Items.Longitude.value());
		double latitude = new Double(Items.Latitude.value());
		Time zTime = SunTimes.getSunsetTimeUTC(zYr, zMonth, zDay, longitude, latitude, SunTimes.CIVIL_ZENITH);
		Date zDate = _gmt.newDate(zYr, zMonth-1, zDay, zTime.getHour(), zTime.getMinute(), zTime.getSecond());
		zDate = cal.lastTick(300, zDate);
		int tHour = cal.getHour(t), zHour = cal.getHour(zDate);
		sunHr[0] = (tHour+1==zHour)?1:0;  
		sunHr[1] = (tHour==zHour)?1:0;  
		sunHr[2] = (tHour-1==zHour)?1:0;
		if(sunHr[1]==1){
			int zMin = cal.getMinute(zDate)/5; 
			sunMin[zMin] = 1;
		}
		// The UGLY block above initializes 15 inputs that identify the sunset time. 
		
		Series idx = new Series(hri,false).append(wdi,mid,sunHr,sunMin);
		// So idx is a vector of 7 + 24 + 12 + 3 + 12 inputs giving the start time + sunset time of that day. 
		
		//System.err.println("---------USING-------"+t);
		//get load		
		Series prevHour = pdb.getLoad("filt", cal.addHoursTo(t, -11), t);
		CheckLoadIntegrity("Input data set", prevHour, cal.addHoursTo(t, -11));
		Series beforePatchPrevHour = prevHour;
		prevHour = patchSpikesLargerThan(prevHour, 500, 2, 10);
		LogSpikes(beforePatchPrevHour, prevHour, cal.addHoursTo(t, -11), t, 500);
		Series[] phComps = prevHour.daub4Separation(_lvls,_db4LD,_db4HD,_db4LR,_db4HR);
		Series[] inputSet = new Series[_lvls+1];
		// This next line picks up the last hour worth of data from each of the H,LH,LLH,... decomposition
		// transforms them and add them at the end of the input vector. The offsets 121/132 correspond to 12 observations (1 hour)
		// and appear to be the last one (since the read from PDB was for hours (t-11 to t)  => 12 * 11 = 132, 10 * 12 = 120
		for(int i = 0;i<_lvls;i++){
			inputSet[i] = idx.append(_norm[i].imageOf((phComps[i].subseries(121,132))));
		}
		//Then differentiate and normalize the low component. So this only deals with the last LLLLLL
		// The first line extract the first value (absolute anchor) and computes a vector of differences t_{i+1} - t_i
		// The second line applies the normalization to the vector just obtained.  Note that it uses two different
		// normalization transforms for the anchor and the differentials (_normAbs and _norm). 
		phComps[_lvls] = phComps[_lvls].suffix(12).prefix(1).append(phComps[_lvls].suffix(12).differentiate().suffix(11));
		inputSet[_lvls] = _normAbs.imageOf(phComps[_lvls].prefix(1)).append(_norm[_lvls].imageOf(phComps[_lvls].suffix(11)))
								.append(idx);
		return inputSet;		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//Define target Vector Set
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * [ldm] This is very messy. It must be cleaned up.
	 * Apparently:
	 * 1. It loads the load data for the time internal (t-10,t+1) (t is an hour number)
	 * 2. It patches that set.
	 * 3. It applies DB4 and obtains (H,LH,LL)  (2 levels, 3 streams) and only retains the last hour for H,LH
	 * 4. It normalizes those and obtains (n(H-suffix),n(LH-suffix),LL)   [last one is not normalized now]
	 * 5. Now it loads the load again, but for the interval (t-11,t)  (one hour earlier)
	 * 6. It does _NOT_ patches the spikes in there?
	 * 7. It applies the DB4 and obtains (H',LH',LL') and it throws away everything but LL'
	 * 8. Then it extracts the last hour from LL and sticks it behind LL'  LL"= LL' ~ suffix(1h,LL)
	 * 9. Finally, it creates a differential of LL" and takes the 1h suffix of that which is then normalized.
	 * 
	 * The output is therefore a NORMALIZED differential of the lowest component of the last hour (hour t)
	 * Overall we get back  (n(H-suffix),n(LH-suffix),diff(LL"-suffix))
	 * The organization makes no sense to me. This seems contrived. 
	 */
	public static Series[] targetSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		Series[] targetSet = new Series[_lvls+1];
		Series load = pdb.getLoad("filt", cal.addHoursTo(t, -10), cal.addHoursTo(t, 1));
		CheckLoadIntegrity("Target data set", load, cal.addHoursTo(t, -10));
		Series beforePatchLoad = load;
		load = patchSpikesLargerThan(load, 500, 2, 10);
		LogSpikes(beforePatchLoad, load, cal.addHoursTo(t, -10), cal.addHoursTo(t, 1), 500);
		Series[] components = load.daub4Separation(_lvls,_db4LD,_db4HD,_db4LR,_db4HR);
		for(int i = 0;i<_lvls;i++){
			targetSet[i] = _norm[i].imageOf(components[i].suffix(12));
		}
		Series prev = pdb.getLoad("filt", cal.addHoursTo(t, -11), cal.addHoursTo(t, -0));
		CheckLoadIntegrity("Target data set", prev, cal.addHoursTo(t, -11));
		prev = prev.daub4Separation(_lvls, _db4LD,_db4HD,_db4LR,_db4HR)[_lvls];
		components[_lvls] = prev.append(components[_lvls].suffix(12));
		targetSet[_lvls] = _norm[_lvls].imageOf(components[_lvls].differentiate().suffix(12));
		return targetSet;
	}
	
	private static Series patchSpikesLargerThan(Series s, 
			double threshold, int minWidth, int maxWidth) throws Exception
	{		
		Series patchedS = new Series(s.array(), false);
		for (int width = minWidth; width < maxWidth; ++width) {

			Series[] diffs = new Series[maxWidth - minWidth]; // the differences using low pass filters
			for (int w = minWidth; w < maxWidth; ++w) {
				int off = w - minWidth;
				diffs[off] = patchedS.minus(patchedS.lowPassFR(w));
			}
			
			double[] array = new double[patchedS.length()]; 
			for(int i=1; i <= patchedS.length(); ++i) {
				if (Math.abs( diffs[width - minWidth].element(i) ) > threshold){
					
					int st = i-1;
					int [] eds = new int[maxWidth - minWidth];
					// Initialize the end points
					for (int eds_i = 0; eds_i < eds.length; ++eds_i)
						eds[eds_i] = i+1;

					// Calculate the end point of spikes for different windows
					for (int w = minWidth; w < maxWidth; ++w) {
						int off = w - minWidth;
						Series diff = diffs[off];	
						
						while (eds[off] <= patchedS.length() &&
								Math.abs(diff.element(eds[off])) > threshold) { 
							++eds[off];
						}
					}
					
					// Find the longest sequence of spikes in the window of the shortest length
					int fitWidth = minWidth;
					for (int w = minWidth + 1; w < maxWidth; ++w) {
						int off = w - minWidth;
						int fitOff = fitWidth - minWidth;
						if (eds[off] > eds[fitOff])
							fitWidth = w;
					}
					
					// Patch the spike with the window of an appropriate length
					int fitOff = fitWidth - minWidth;
					int ed = eds[fitOff];
					if (ed > patchedS.length()) {
						// end of the spike point is out of range
						ed = patchedS.length();
						array[ed-1] = patchedS.element(st) + (ed-st)*(patchedS.element(st)-patchedS.element(st-1));
					}
					else 
						// end of the spike point is in range
						array[ed-1] = patchedS.element(ed);
					
					// Patch the spike
					for(int j=st+1,k=1;j<=ed;j++,k++)
						array[j-1] = patchedS.element(st)+ k*(array[ed-1]-patchedS.element(st))/(ed-st);
					
					i = eds[fitOff];
				}
				else {
					// Normal point
					array[i-1] = patchedS.element(i);
				}
			} // int i
			patchedS = new Series(array,false);
		}	// int width
			
		return patchedS;
	}
	
	private static void LogSpikes(Series unpatched, Series patched, 
			Date from, Date to, double threshold) throws Exception
	{
		int i = 1;
		do {
			// Find the start point of a spike
			int st = i;
			
			while ( st <= unpatched.length() && 
					Math.abs(unpatched.element(st) - patched.element(st)) <= threshold )
				++st;
			
			if (st > unpatched.length())
				break;
			
			// Find the end point of a spike
			int ed = st;
			while ( ed <= unpatched.length() &&
					Math.abs(unpatched.element(ed) - patched.element(ed)) > threshold )
				++ed;
			
			if (ed > unpatched.length())
				ed = unpatched.length();
			else
				ed = ed - 1;
			
			// Log the spike
			Calendar cal = new Calendar();
			StringBuffer sb = new StringBuffer(512);
			sb.append("Spike starting from ").append(cal.addMinutesTo(from, st*5));
			sb.append(" to ").append(cal.addMinutesTo(from, ed*5)).append(":\n");
			sb.append("\tBefore patch:\t");
			for (int t = st; t <= ed; ++t)
				sb.append(unpatched.element(t)).append(", ");
			sb.append("\n\tAfter patch:\t");
			for (int t = st; t <= ed; ++t)
				sb.append(patched.element(t)).append(", ");
			
			String methodName = VSTLFTrainer.class.getDeclaredMethod("LogSpikes", new Class[]{Series.class, Series.class, Date.class, Date.class, Double.TYPE}).getName();
			MessageCenter.getInstance().put(
					new LogMessage(Level.WARNING, VSTLFTrainer.class.getName(), methodName, sb.toString()));
			
			//
			i = ed + 1;
		} while (i <= unpatched.length());
	}
	
	// 
	private static void CheckLoadIntegrity(String tag, Series s, Date strt) throws Exception
	{
		Calendar cal = new Calendar();
		strt = cal.beginBlock(300,strt);
		Double nan = Double.NaN;
		for (int i = 0; i < s.length(); ++i) {
			if (nan.equals(s.element(i+1))) {
				//String errMsg = "Load on " + cal.addSecondsTo(strt, (i+1)*300) + " does not exist";
				String errMsg = tag + ": traning data on " + cal.addSecondsTo(strt, (i+1)*300) + " does not exist. Try to adjust the training range.";
				//System.err.println(errMsg);
				throw new Exception(errMsg);
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//Constants
	//////////////////////////////////////////////////////////////////////////////////////
	
	static double[] _db8LD = {0.2303778133, 0.7148465706, 0.6308807679, -0.0279837694, -0.1870348117, 0.0308413818, 0.0328830117, -0.0105974018};
	static double[] _db8HD = {-0.0105974018, -0.0328830117, 0.0308413818, 0.1870348117, -0.0279837694, -0.6308807679, 0.7148465706, -0.2303778133};
	static double[] _db8LR = {-0.0105974018, 0.0328830117, 0.0308413818, -0.1870348117, -0.0279837694, 0.6308807679, 0.7148465706, 0.2303778133};
	static double[] _db8HR = {-0.2303778133, 0.7148465706, -0.6308807679, -0.0279837694, 0.1870348117, 0.0308413818, -0.0328830117, -0.0105974018};
		
	static double _rootThree = Math.sqrt(3);
    static double _fourRootTwo = 4*Math.sqrt(2);   
    static double[] _db4LD = {(1 + _rootThree)/_fourRootTwo, (3 + _rootThree)/_fourRootTwo,
                      (3 - _rootThree)/_fourRootTwo, (1 - _rootThree)/_fourRootTwo};
    static double[] _db4HD = {_db4LD[3], -_db4LD[2], _db4LD[1], -_db4LD[0]};
    static double[] _db4LR = {_db4LD[3], _db4LD[2], _db4LD[1], _db4LD[0]};
    static double[] _db4HR = {_db4HD[3], _db4HD[2], _db4HD[1], _db4HD[0]};
    
 }
