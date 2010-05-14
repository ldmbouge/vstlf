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
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.*;
import edu.uconn.vstlf.neuro.*;

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
		
	public static double[][] test (String loadFile, Date stTest, Date edTest, int lo, int up){
		try{
			double[][] result = new double[12][];
			for(int offs=lo;offs<=up;offs++){
				System.out.println("Testing System for offset: "+ 5*offs +" minutes." );
				///////////////////////////////////////////////////////////////////////////////////
				//Initialize db and cal et cetera
				///////////////////////////////////////////////////////////////////////////////////
				Calendar cal = new Calendar("America/New_York");
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
				_normAbs = new NormalizingFunction(5000,28000,0,1);
				_denormAbs = new NormalizingFunction(0,1,5000,28000);
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
										  .daub4Separation(_lvls, _db8LD,_db8HD,_db8LR,_db8HR)[_lvls];
					out[_lvls] = out[_lvls].undifferentiate(prev.suffix(1).element(1));
					pred = pred.plus(out[_lvls]);
					Series act = loadHist.getLoad("filt", cal.addHoursTo(ts, -10), cal.addHoursTo(ts, 1));
					
					
					//Separate the actual load.////////////
					Series[] real = act.daub4Separation(_lvls, _db8LD,_db8HD,_db8LR,_db8HR);
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
			for(int offs=lo;offs<=up;offs++){
				System.out.println("Training System for offset: "+ 5*offs +" minutes." );
				///////////////////////////////////////////////////////////////////////////////////
				//Initialize db and cal et cetera
				///////////////////////////////////////////////////////////////////////////////////
				Calendar cal = new Calendar("America/New_York");
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
				_normAbs = new NormalizingFunction(5000,28000,0,1);
				_denormAbs = new NormalizingFunction(0,1,5000,28000);
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
		Time zTime = SunTimes.getSunsetTimeUTC(zYr, zMonth, zDay, -72.6166667, 42.2041667, SunTimes.CIVIL_ZENITH);
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
		for(int w = 2;w<10;w++)prevHour = prevHour.patchSpikesLargerThan(500, w);
		Series[] phComps = prevHour.daub4Separation(_lvls,_db4LD,_db4HD,_db4LR,_db4HR);
		Series[] inputSet = new Series[_lvls+1];
		for(int i = 0;i<_lvls;i++){
			inputSet[i] = idx.append(_norm[i].imageOf((phComps[i].subseries(121,132))));
		}
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
		for(int w = 2;w<10;w++)load = load.patchSpikesLargerThan(500, w);
		Series[] components = load.daub4Separation(_lvls,_db4LD,_db4HD,_db4LR,_db4HR);
		for(int i = 0;i<_lvls;i++){
			targetSet[i] = _norm[i].imageOf(components[i].suffix(12));
		}
		Series prev = pdb.getLoad("filt", cal.addHoursTo(t, -11), cal.addHoursTo(t, -0)).daub4Separation(_lvls, _db4LD,_db4HD,_db4LR,_db4HR)[_lvls];
		components[_lvls] = prev.append(components[_lvls].suffix(12));
		targetSet[_lvls] = _norm[_lvls].imageOf(components[_lvls].differentiate().suffix(12));
		return targetSet;
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
    
    
	
	public static void main(String[] args){
		_lo = 0;//Integer.parseInt(args[0]);
		_up = 0;//Integer.parseInt(args[1]);
		Calendar cal = new Calendar("America/New_York");
		Date s = cal.newDate(2007, 0, 2, 0, 0, 0);
		Date t = cal.newDate(2008, 0, 1, 0, 0, 0);
		//train("5m_2007-Mar2009.pod",s,t,_lo,_up);
		//s = cal.newDate(2008, 0, 1, 0, 0, 0);
		//t = cal.newDate(2008, 6, 1, 0, 0, 0);
		//s = cal.newDate(2008, 7, 1, 0, 0, 0);
		//t = cal.newDate(2008, 9, 1, 0, 0, 0);
		double[][] r = test("5m_2007-Mar2009.pod",s,t,_lo,_up); //5m_2007-Mar2009
		for(int off=0;off<1;off++){
			for(int i=0;i<12;i++){
			//	System.out.print(r[off][i]+"\t");
			}
			System.out.println();
		}
	}


	
	
	
	
	
}
