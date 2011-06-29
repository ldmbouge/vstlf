package edu.uconn.vstlf.prediction;

import java.util.Date;
import java.util.Vector;

import com.web_tomorrow.utils.suntimes.SunTimesException;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

/*
 * A load series is a series of load
 * The last point in the series has the currentTime stamp
 */
public class LoadSeries extends InputBlock {

	Series load_;
	Date currTime_;
	Calendar cal_;
	
	public LoadSeries(Series load, Date time, Calendar cal) {
		load_ = load;
		currTime_ = time;
		cal_ = cal;
	}
	
	/*
	 * The load clock derives the time indices
	 * and the decomposed inputs
	 * @see edu.uconn.vstlf.prediction.InputBlock#deriveInputBlocks()
	 */
	@Override
	public Vector<InputBlock> deriveInputBlocks() throws SunTimesException {
		Vector<InputBlock> drvBlks = new Vector<InputBlock>();
		drvBlks.add(new HourIndexBlock(currTime_, cal_));
		drvBlks.add(new WeekIndexBlock(currTime_, cal_));
		drvBlks.add(new MonthIndexBlock(currTime_, cal_));
		drvBlks.add(new SunsetIndexBlock(currTime_, cal_));

		return drvBlks;
	}

	@Override
	public double[] getInput() {
		// TODO Auto-generated method stub
		return null;
	}

}
