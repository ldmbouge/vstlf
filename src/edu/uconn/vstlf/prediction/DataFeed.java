package edu.uconn.vstlf.prediction;

import java.util.Date;
import java.util.TreeMap;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.NormalizingFunction;
import edu.uconn.vstlf.data.doubleprecision.Series;

public class DataFeed {
	private DaubSpec dbSpec_;
	private LoadSeries loadSeries_;
	private Calendar cal_;
	private Date curTime_;
	
	private TreeMap<Integer, Series[]> decompLoads_ = new TreeMap<Integer, Series[]>();

	/*
	 * get decomposed load from 
	 * curTime_.addHours(-(shiftHour+nHours)) to
	 * curTime_.addHours(-shiftHour)
	 */
	public Series[] getDecomposedLoads(int shiftHour, int nHours) throws Exception 
	{ 
		Series[] dcLoads = decompLoads_.get(shiftHour);
		if (dcLoads == null) {
			dcLoads = decomposeLoads(shiftHour);
			decompLoads_.put(shiftHour, dcLoads);
		}
		
		Series[] rslt = new Series[dcLoads.length];
		for (int i = 0; i < dcLoads.length; ++i) {
			rslt[i] = dcLoads[i].suffix(12*nHours);
		}
		
		return rslt;
	}
	
	public Date getCurTime() { return curTime_; }
	public Calendar getCalendar() { return cal_; }
	
	public int getNDecompLvls() { return dbSpec_.getNumLevels(); }
	
	static private NormalizingFunction 
	_normAbs = new NormalizingFunction(Items.getMinimumLoad(),Items.getMaximumLoad(),0,1),
	_denormAbs = new NormalizingFunction(0,1,Items.getMinimumLoad(),Items.getMaximumLoad());
	static private NormalizingFunction[] _norm = {new NormalizingFunction(-500,500,0,1), //h
				  new NormalizingFunction(-500,500,0,1),	//lh
				  new NormalizingFunction(-.1,.1,0,1)}, 
		 _denorm = {new NormalizingFunction(0,1,-500,500),
			    new NormalizingFunction(0,1,-500,500),//ditto
			    new NormalizingFunction(0,1,-.1,.1)};

	
	static public NormalizingFunction[] getNormFuncs() { return _norm; }
	static public NormalizingFunction[] getDenormFuncs() { return _denorm; }
	static public NormalizingFunction getAbsNormFunc() { return _normAbs; }
	static public NormalizingFunction getAbsDenormFunc() { return _denormAbs; }
	
	public DataFeed(LoadSeries loadSeries, DaubSpec dbSpec) throws Exception
	{
		loadSeries_ = loadSeries;		
		dbSpec_ = dbSpec;
		curTime_ = loadSeries.getCurTime();
		cal_ = loadSeries.getCal();
	}
	
	private Series[] decomposeLoads(int shiftHour) throws Exception
	{
		Date ed = cal_.addHoursTo(curTime_, shiftHour);
		Date st = cal_.addHoursTo(curTime_, -11 + shiftHour);
		if (shiftHour > 0) 
			throw new Exception("Cannot decompose the load at " + ed  + ", load only available at " + curTime_);
		
		//get load		
		Series prevHour = loadSeries_.getSubSeries(st, ed).getLoad();
		
		int nlvls = dbSpec_.getNumLevels();
		Series[] decompLoads = prevHour.daub4Separation(nlvls, 
				dbSpec_.getDB4LD(), dbSpec_.getDB4HD(), dbSpec_.getDB4LR(), dbSpec_.getDB4HR());
		return decompLoads;
	}
	
}
