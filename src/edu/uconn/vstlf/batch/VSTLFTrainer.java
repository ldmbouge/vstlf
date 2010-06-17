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

import java.util.Date;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.*;
import edu.uconn.vstlf.neuro.*;
import edu.uconn.vstlf.config.Items;

import com.web_tomorrow.utils.suntimes.*;

public class VSTLFTrainer {		
	static private Logger trainLog = Logger.getLogger("trainLog");
	
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
		
	public static double[][] test (String loadFile, Date stTest, Date edTest, int lo, int up){
		try{
			double[][] result = new double[12][];
			for(int offs=lo;offs<=up;offs++){
				System.out.println("Testing System for offset: "+ 5*offs +" minutes." );
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
				@SuppressWarnings("unused")
				long t0 = System.currentTimeMillis();
				///////////////////////////////////////////////////////////////////////////////
				//Run forecast test
				//////////////////////////////////////////////////////////////////////////////
				System.out.println("Running forecast test.  ");
				Vector<Series> errV = new Vector<Series>();
				Vector<Series> difV = new Vector<Series>();
				System.out.println("Forecasting from "+cal.string(ts)+" to "+ cal.string(ed));
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
				System.out.println(k+" forecasts made.");
				Series aveErr = new MeanFunction().imageOf(errs);
				Series stdErr = new StDevFunction().imageOf(errs);
				Series aveDif = new MeanFunction().imageOf(difs);
				Series stdDif = new StDevFunction().imageOf(difs);
				System.out.println("Mean Err: \t" + aveErr);
				System.out.println("std(Err): \t" + stdErr);
				System.out.println("Mean Dif: \t" + aveDif);
				System.out.println("std(Dif): \t" + stdDif);
				
				for(int lns=0;lns<8;lns++)System.out.println();		
				//////////////////////////////////////////////////////////////////////////////////////
				//Finish up the iteration.
				//////////////////////////////////////////////////////////////////////////////////////
				//wdb.close();
				loadHist.close();
				result[offs] = aveErr.array();
				return result;
			}
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
			Handler hf = new FileHandler("train.log");
			trainLog.addHandler(hf);
			
			for(int offs=lo;offs<=up;offs++){
				System.out.println("Training System for offset: "+ 5*offs +" minutes." );
				///////////////////////////////////////////////////////////////////////////////////
				//Initialize db and cal et cetera
				///////////////////////////////////////////////////////////////////////////////////
				Calendar cal = new Calendar();
				PowerDB loadHist = new PerstPowerDB(loadFile,300);
				loadHist.open();
				///////////////////////////////////////////////////////////////////////////////////
				//Define test parameters//
				//////////////////////////////////////////////////////////////////////////////////
				Date ts = stTrain;    				//st train
				Date curr = edTrain;					//ed train
				
				ts = cal.addMinutesTo(ts, 5*offs);
				curr = cal.addMinutesTo(curr, 5*offs);
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
				int[] times = {th,tlh,tllh,tlllh,tllll};
				double[] errors = {eh,elh,ellh,elllh,ellll};
				_norm = new NormalizingFunction[_lvls+1];
				_denorm = new NormalizingFunction[_lvls+1];
				int[] sec = new int[_lvls+1];
				double[] mse = new double[_lvls+1];
				int[][] lyrSz = new int[_lvls+1][];
				for(int i = 0;i<_lvls;i++){
					_norm[i] = normalizers[i];
					_denorm[i] = denormalizers[i];
					sec[i] = times[i];
					mse[i] = errors[i];
					lyrSz[i] = layerSizes[i];
				}
				_norm[_lvls] = normalizers[4];
				_denorm[_lvls] = denormalizers[4];
				double minload = new Double(Items.MinLoad.value());
				double maxload = new Double(Items.MaxLoad.value());
				_normAbs = new NormalizingFunction(minload,maxload,0,1);
				_denormAbs = new NormalizingFunction(0,1,minload,maxload);
				sec[_lvls] = times[4];
				mse[_lvls] = errors[4];
				lyrSz[_lvls] = layerSizes[4];
				
				////////////////////////////////////////////////////////////////////////////////
				//Build training set
				/////////////////////////////////////////////////////////////////////////////////
				Vector<Series[]>inS = new Vector<Series[]>(), tgS = new Vector<Series[]>();
				System.out.println("Building training set from "+ cal.string(ts)+" to "+ cal.string(curr));
				while(ts.before(curr)){
					//	System.err.println((++xxx)+": Added "+ cal.string(ts));
						tgS.add(targetSetFor(ts,cal,loadHist));
						inS.add(inputSetFor(ts, cal, loadHist));
						//System.err.println(ts);
						Series[] set = inS.lastElement();
						for(int i = 0;i<set.length;i++){
							if(set[i].countOf(Double.NaN)>0){
								System.out.println(ts);
								throw new Exception("WTF?");
							}
						}
						ts = cal.addMinutesTo(ts, 60);
					}
				if(inS.size()!=tgS.size()) throw new Exception("#tg!=#in");
				Series[][] inputS = new Series[inS.size()][], targS = new Series[tgS.size()][];
				for(int i=0;i<targS.length;i++){
					inputS[i] = inS.elementAt(i);
					targS[i] = tgS.elementAt(i);
				}
				System.out.println(inputS.length);
				System.out.println("Training set contains "+inS.size()+" points.  ");
				///////////////////////////////////////////////////////////////////////////////
				//Make ANNs and Train
				///////////////////////////////////////////////////////////////////////////////
				ANNBank anns;
				try{
					anns= new ANNBank("bank"+offs+".ann");
				}
				catch(Exception e){
					anns = new ANNBank(lyrSz);
				}
				long t0 = System.currentTimeMillis();
				anns.train(inputS, targS, sec,mse);
				System.out.println("after "+ (System.currentTimeMillis()-t0));
				anns.save("bank"+offs+".ann");
				loadHist.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//Define Input Vector Set
	////////////////////////////////////////////////////////////////////////////////////////////
	public static Series[] inputSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		///Time Index
		double[] wdi = new double[7];
		double[] hri = new double[24];
		double[] mid = new double[12];
		hri[cal.getHour(t)] = 1;
		wdi[cal.getDayOfWeek(t)-1] = 1;
		mid[cal.getMonth(t)] = 1;
		
		
		
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
		sunHr[0] = (tHour+1==zHour)?1:0;  sunHr[1] = (tHour==zHour)?1:0;  sunHr[2] = (tHour-1==zHour)?1:0;
		if(sunHr[1]==1){
			int zMin = cal.getMinute(zDate)/5; sunMin[zMin] = 1;
		}
		Series idx = new Series(hri,false)
			 .append(new Series(wdi,false))
			 .append(new Series(mid,false))
			 .append(new Series(sunHr,false))
			 .append(new Series(sunMin,false));
		
		//System.err.println("---------USING-------"+t);
		//get load		
		Series prevHour = pdb.getLoad("filt", cal.addHoursTo(t, -11), t);
		CheckLoadIntegrity("Input data set", prevHour, cal.addHoursTo(t, -11));
		Series beforePatchPrevHour = prevHour;
		prevHour = patchSpikesLargerThan(prevHour, 500, 2, 10);
		LogSpikes(beforePatchPrevHour, prevHour, cal.addHoursTo(t, -11), t, 500, trainLog);
		Series[] phComps = prevHour.daub4Separation(_lvls,_db4LD,_db4HD,_db4LR,_db4HR);
		Series[] inputSet = new Series[_lvls+1];
		for(int i = 0;i<_lvls;i++){
			inputSet[i] = idx.append(_norm[i].imageOf((phComps[i].subseries(121,132))));
		}//Then differentiate and normalize the low component.
		phComps[_lvls] = phComps[_lvls].suffix(12).prefix(1).append(phComps[_lvls].suffix(12).differentiate().suffix(11));
		inputSet[_lvls] = _normAbs.imageOf(phComps[_lvls].prefix(1)).append(_norm[_lvls].imageOf(phComps[_lvls].suffix(11)))
								.append(idx);
		return inputSet;		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//Define target Vector Set
	///////////////////////////////////////////////////////////////////////////////////////////
	public static Series[] targetSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		Series[] targetSet = new Series[_lvls+1];
		Series load = pdb.getLoad("filt", cal.addHoursTo(t, -10), cal.addHoursTo(t, 1));
		CheckLoadIntegrity("Target data set", load, cal.addHoursTo(t, -10));
		Series beforePatchLoad = load;
		load = patchSpikesLargerThan(load, 500, 2, 10);
		LogSpikes(beforePatchLoad, load, cal.addHoursTo(t, -10), cal.addHoursTo(t, 1), 500, trainLog);
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
			Date from, Date to, double threshold, Logger lgr) throws Exception
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
			String s = "Spike starting from " + cal.addMinutesTo(from, st*5) + " to " +
				cal.addMinutesTo(from, ed*5) + ":\n";
			s = s + "\tBefore patch:\t";
			for (int t = st; t <= ed; ++t)
				s = s + unpatched.element(t) + ", ";
			s = s + "\n\tAfter patch:\t";
			for (int t = st; t <= ed; ++t)
				s = s + patched.element(t) + ", ";
			
			lgr.warning(s);
			
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
