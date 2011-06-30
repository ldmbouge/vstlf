package edu.uconn.vstlf.block;

import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

public class HourIndexBlock extends InputBlock {

	private double[] hri_ = new double[24];

	public HourIndexBlock(Date t, Calendar cal) {
		hri_[cal.getHour(t)] = 1;
	}
	
	@Override
	public Series getInput() throws Exception {
		// TODO Auto-generated method stub
		return new Series(hri_, false);
	}

}
