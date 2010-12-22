package edu.uconn.vstlf.realtime;

import java.util.Date;

import com.web_tomorrow.utils.suntimes.SunTimes;
import com.web_tomorrow.utils.suntimes.Time;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.NormalizingFunction;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.PowerDB;

public class DataFeed {
	private Calendar _gmt;
	NormalizingFunction[] _norm = {new NormalizingFunction(-500,500,0,1), //h
				  new NormalizingFunction(-500,500,0,1),	//lh
				  new NormalizingFunction(-.1,.1,0,1)}, 
		 _denorm = {new NormalizingFunction(0,1,-500,500),
			    new NormalizingFunction(0,1,-500,500),//ditto
			    new NormalizingFunction(0,1,-.1,.1)};

    NormalizingFunction 
		_normAbs,
		_denormAbs;
    
    static int _lvls = 2;
    
	static double _rootThree = Math.sqrt(3);
    static double _fourRootTwo = 4*Math.sqrt(2);   
    static double[] _db4LD = {(1 + _rootThree)/_fourRootTwo, (3 + _rootThree)/_fourRootTwo,
                      (3 - _rootThree)/_fourRootTwo, (1 - _rootThree)/_fourRootTwo};
    static double[] _db4HD = {_db4LD[3], -_db4LD[2], _db4LD[1], -_db4LD[0]};
    static double[] _db4LR = {_db4LD[3], _db4LD[2], _db4LD[1], _db4LD[0]};
    static double[] _db4HR = {_db4HD[3], _db4HD[2], _db4HD[1], _db4HD[0]};
    
    public DataFeed()
    {
		_gmt = new Calendar("GMT");
		double minload = Items.getMinimumLoad();
		double maxload = Items.getMaximumLoad();
		_normAbs = new NormalizingFunction(minload,maxload,0,1);
		_denormAbs = new NormalizingFunction(0,1,minload,maxload);
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
	
	public Series[] targetSetFor(Date t,Calendar cal, PowerDB pdb)throws Exception{
		Series[] targetSet = new Series[_lvls+1];
		Series load = pdb.getLoad("filt", cal.addHoursTo(t, -10), cal.addHoursTo(t, 1));
		//WTF?for(int w = 2;w<10;w++)load = load.patchSpikesLargerThan(500, w);
		Series[] components = load.daub4Separation(_lvls,_db4LD,_db4HD,_db4LR,_db4HR);
		for(int i = 0;i<_lvls;i++){
			targetSet[i] = _norm[i].imageOf(components[i].suffix(12));
		}
		Series prev = pdb.getLoad("filt", cal.addHoursTo(t, -11), cal.addHoursTo(t, -0)).daub4Separation(_lvls, _db4LD,_db4HD,_db4LR,_db4HR)[_lvls];
		components[_lvls] = prev.append(components[_lvls].suffix(12));
		targetSet[_lvls] = _norm[_lvls].imageOf(components[_lvls].differentiate().suffix(12));
		return targetSet;
	}
	
	public Series denormOut(Series out[], Series prev) throws Exception
	{
		Series pred = new Series(12);
		for(int i = 0;i<2;i++){
			out[i] = _denorm[i].imageOf(out[i]);
			pred = pred.plus(out[i]);
		}
		out[2] = (_denorm[2].imageOf(out[2]));
		prev = prev.daub4Separation(2, _db4LD,_db4HD,_db4LR,_db4HR)[2];
		out[2] = out[2].undifferentiate(prev.suffix(1).element(1));
		pred = pred.plus(out[2]);
		return pred;
	}
}
