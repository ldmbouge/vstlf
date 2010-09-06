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

package edu.uconn.vstlf.realtime;
import java.util.Date;
//import java.util.Vector;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.database.*;
import edu.uconn.vstlf.neuro.*;
import edu.uconn.vstlf.config.Items;
import com.web_tomorrow.utils.suntimes.*;

public class FiveMinuteProcess extends Thread {
	private final PCBuffer<VSTLF5MPoint> _input;
	private final MessageCenter _notif;
	//private Vector<VSTLF5MPoint> _window;
	private PowerDB _db;
	private ANNBank[] _annBanks;
	private Calendar _cal,_gmt;
	NormalizingFunction[] _norm = {new NormalizingFunction(-500,500,0,1), //h
		   								  new NormalizingFunction(-500,500,0,1),	//lh
		   								  new NormalizingFunction(-.1,.1,0,1)}, 
		   						 _denorm = {new NormalizingFunction(0,1,-500,500),
										    new NormalizingFunction(0,1,-500,500),//ditto
										    new NormalizingFunction(0,1,-.1,.1)};
    NormalizingFunction 
    	_normAbs,
		_denormAbs;
	
												
    static double _rootThree = Math.sqrt(3);
    static double _fourRootTwo = 4*Math.sqrt(2);   
    static double[] _db4LD = {(1 + _rootThree)/_fourRootTwo, (3 + _rootThree)/_fourRootTwo,
                      (3 - _rootThree)/_fourRootTwo, (1 - _rootThree)/_fourRootTwo};
    static double[] _db4HD = {_db4LD[3], -_db4LD[2], _db4LD[1], -_db4LD[0]};
    static double[] _db4LR = {_db4LD[3], _db4LD[2], _db4LD[1], _db4LD[0]};
    static double[] _db4HR = {_db4HD[3], _db4HD[2], _db4HD[1], _db4HD[0]};
   
    private boolean _doFilter;
	boolean _useSimDay;
	double _filterThreshold;

	FiveMinuteProcess(MessageCenter notif, PCBuffer<VSTLF5MPoint> buf, PowerDB db) {
		_input = buf;		
		_notif = notif;
		_db = db;
		_cal = Items.makeCalendar();
		_gmt = new Calendar("GMT");
		_annBanks = new ANNBank[12];
		for(int i = 0;i<12;i++){
			try{
				_annBanks[i] = new ANNBank("anns/bank"+i+".ann");
			}catch(Exception e){
				_notif.put(new VSTLFRealTimeExceptionMessage(e));
			}
		}
		double minload = Items.getMinimumLoad();
		double maxload = Items.getMaximumLoad();
		_normAbs = new NormalizingFunction(minload,maxload,0,1);
		_denormAbs = new NormalizingFunction(0,1,minload,maxload);
		_doFilter = true;
		_filterThreshold = 200;
		_useSimDay = false;
	}
	
	public void run() {
		boolean done = false;
		while(!done) {
			VSTLF5MPoint thePoint = _input.consume();						//Get next 5mPoint
			if (!thePoint.isValid()) {				
				break;
			}
			Date t = thePoint.getStamp(); double v = thePoint.getValue(); int n = thePoint.getNumObs();
			_db.addLoad("raw", t, v);												//Store it in the DB
			_db.commit();
			macroFilter(t);
			_notif.put(new FiveMinMessage(t,v,n));
			doUpdate();														//Update on 1hourAgo
			doPrediction();
		}
	}
	
	private void checkData(Date from, Series s){
		Double nan = new Double(Double.NaN);
		double[] a = s.array(false);
		for(int i=0;i<s.length();i++){
			if(nan.equals(a[i])){
				a[i] = 0;
				_notif.put(new MessageMissing5m(_cal.addMinutesTo(from, (i+1)*5)));
			}
		}
	}
	
	private void macroFilter(Date t){
		try{
			Series older = _db.getLoad("filt", _cal.addHoursTo(t, -4), _cal.addHoursTo(t, -3));
			Series recent = _db.getLoad("raw", _cal.addHoursTo(t, -3), t);
			checkData(_cal.addHoursTo(t, -3),recent);
			Series orig = older.append(recent);
			Series out = orig;
			if(_doFilter){
				//System.out.println("Filtering "+orig.length());
				out = orig.patchSpikesLargerThan(_filterThreshold);
				//System.out.format("%s\n%s\n",orig,out);
			}
			for(int i = 36;i<=out.length();i++) {
				_db.addLoad("filt", _cal.addMinutesTo(_cal.addHoursTo(t, -4),5*i), out.element(i));
				if(out.element(i) != orig.element(i)){
					_notif.put(new VSTLF5mRefinementMessage(_cal.addMinutesTo(_cal.addHoursTo(t, -4),5*i), orig.element(i), out.element(i)));
				}
			}
			_db.commit();
		}catch(Exception e){
			_notif.put(new VSTLFRealTimeExceptionMessage(e));
			try{
				Series recent = _db.getLoad("raw", _cal.addHoursTo(t, -2), t);
				for(int i = 1;i<=recent.length();i++) 
					_db.addLoad("filt", _cal.addMinutesTo(_cal.addHoursTo(t, -2),5*i), recent.element(i));
				_db.commit();
			}catch(Exception x){
				_notif.put(new VSTLFRealTimeExceptionMessage(
						new Exception("Fatal Exception, 'filtered' Database did not accept data.")));
			}
			
		}
	}
	
	
	
	/**
	 * Calls the ANN to perform a prediction @ the 5m mark. Predict and save the prediction
	 * back in PERST. 
	 * @param stamp
	 */
	private void doPrediction() {
		try{
			Date stamp = _db.last("filt");
			// First do the prediction.
			int off = _cal.getMinute(stamp);
			//System.out.println("Predicting @@@@@@@@@@@@@@@@@@@@@@@@@@\t"+stamp);
			Series[] out = _annBanks[off/5].execute(inputSetFor(stamp,_cal,_db));
			Series pred = new Series(12);
			for(int i = 0;i<2;i++){
				out[i] = _denorm[i].imageOf(out[i]);
				pred = pred.plus(out[i]);
			}
			out[2] = (_denorm[2].imageOf(out[2]));
			Series prev = _db.getLoad("filt", _cal.addHoursTo(stamp, -11), _cal.addHoursTo(stamp, 0));
			checkData(_cal.addHoursTo(stamp, -11),prev);
			prev = prev.daub4Separation(2, _db4LD,_db4HD,_db4LR,_db4HR)[2];
			out[2] = out[2].undifferentiate(prev.suffix(1).element(1));
			pred = pred.plus(out[2]);
			_notif.put(new PredictionMessage(stamp,pred.array()));
		}
		catch(Exception e){
			_notif.put(new VSTLFRealTimeExceptionMessage(e));
		}
	}
	
	private void doUpdate(){
		try{
			//Update on the most recent entire hour
			Date stamp = _cal.addHoursTo(_db.last("filt"), -1);
			//execute
			int off = _cal.getMinute(stamp);
			_annBanks[off/5].execute(inputSetFor(stamp,_cal,_db));
			//and then back propagate
			_annBanks[off/5].update(targetSetFor(stamp,_cal,_db));
		}
		catch(Exception e){
			_notif.put(new VSTLFRealTimeExceptionMessage(e));
		}
		
	}
	
	public Series[] inputSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		///Time Index
		double[] wdi = new double[7];
		double[] hri = new double[24];
		double[] mid = new double[12];
		hri[cal.getHour(t)] = 1;
		wdi[cal.getDayOfWeek(t)-1] = 1;
		mid[cal.getMonth(t)] = 1;
		
		//sunset stuff
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
		
		//get load		
		Series prevHour = pdb.getLoad("filt", cal.addHoursTo(t, -11), t);
		//WTF?for(int w = 2;w<10;w++)prevHour = prevHour.patchSpikesLargerThan(500, w);
		Series[] phComps = prevHour.daub4Separation(2,_db4LD,_db4HD,_db4LR,_db4HR);
		Series[] inputSet = new Series[3];
		for(int i = 0;i<2;i++){
			inputSet[i] = idx.append(_norm[i].imageOf((phComps[i].subseries(121,132))));
		}
		phComps[2] = phComps[2].suffix(12).prefix(1).append(phComps[2].suffix(12).differentiate().suffix(11));
		inputSet[2] = _normAbs.imageOf(phComps[2].prefix(1)).append(_norm[2].imageOf(phComps[2].suffix(11)))
								.append(idx);
		return inputSet;		
	}
	
	public Series[] targetSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		Series[] targetSet = new Series[3];
		Series load = pdb.getLoad("filt", cal.addHoursTo(t, -10), cal.addHoursTo(t, 1));
		//WTF?for(int w = 2;w<10;w++)load = load.patchSpikesLargerThan(500, w);
		Series[] components = load.daub4Separation(2,_db4LD,_db4HD,_db4LR,_db4HR);
		for(int i = 0;i<2;i++){
			targetSet[i] = _norm[i].imageOf(components[i].suffix(12));
		}
		Series prev = pdb.getLoad("filt", cal.addHoursTo(t, -11), cal.addHoursTo(t, -0)).daub4Separation(2, _db4LD,_db4HD,_db4LR,_db4HR)[2];
		components[2] = prev.append(components[2].suffix(12));
		targetSet[2] = _norm[2].imageOf(components[2].differentiate().suffix(12));
		return targetSet;
	}
	
	
	public void useFilter(boolean on){
		_doFilter = on;
	}
	
	public void setFilterThreshold(double t){
		_filterThreshold = t;
	}
}
