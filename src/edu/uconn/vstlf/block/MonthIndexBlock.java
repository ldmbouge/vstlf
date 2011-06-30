package edu.uconn.vstlf.block;

import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

public class MonthIndexBlock extends InputBlock {

	private double[] mid_ = new double[12];

	public MonthIndexBlock(Date t, Calendar cal) {
		mid_[cal.getMonth(t)] = 1;	
	}
	

	@Override
	public Series getInput() throws Exception {
		return new Series(mid_, false);
	}

}
