package edu.uconn.vstlf.block;

import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

public class WeekIndexBlock extends InputBlock {

	private double[] wdi_ = new double[7];

	public WeekIndexBlock(Date t, Calendar cal) {
		wdi_[cal.getDayOfWeek(t)-1] = 1;
	}
	

	@Override
	public Series getInput() throws Exception {
		// TODO Auto-generated method stub
		return new Series(wdi_, false);
	}

}
