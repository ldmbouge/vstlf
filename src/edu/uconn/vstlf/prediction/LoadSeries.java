package edu.uconn.vstlf.prediction;

import java.util.Date;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

/*
 * A load series is a series of load
 * The last point in the series has the currentTime stamp
 */
public class LoadSeries {

	Series load_;
	Date currTime_;
	Calendar cal_;
	
	public LoadSeries(Series load, Date time, Calendar cal) {
		load_ = load;
		currTime_ = time;
		cal_ = cal;
	}
	
	public Calendar getCal() { return cal_; }
	public Date getCurTime() { return currTime_; }
	public Series getLoad() { return load_; }
	
	/*
	 * get the load starting from st to ed. The interval is half-closed (st, ed]
	 * which means the load at st is not in the returned results. But the load
	 * at ed is.
	 */
	Series getLoad(Date st, Date ed) throws Exception
	{
		if (st.after(currTime_) || ed.after(currTime_))
			throw new Exception("date out of range");
		
		long stOff = (currTime_.getTime()-st.getTime())/1000;
		long edOff = (currTime_.getTime()-ed.getTime())/1000;
		
		int stIndex = load_.length()-(int)(stOff/300)+1;
		int edIndex = load_.length()-(int)(edOff/300);		
		
		return load_.subseries(stIndex, edIndex);
	}
	
	// get a sub series until time t
	LoadSeries getSubSeries(Date t) throws Exception
	{
		if (t.after(currTime_))
			throw new Exception("Cannot get sub load series until " + t + " from a series until " + currTime_);
		
		long offSecs = (currTime_.getTime() - t.getTime())/1000;
		int offset = (int)(offSecs/300);
		Series subS = load_.prefix(load_.length()-offset);
		return new LoadSeries(subS, t, cal_);
	}
}
