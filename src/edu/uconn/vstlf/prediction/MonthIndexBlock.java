package edu.uconn.vstlf.prediction;

import java.util.Date;
import java.util.Vector;

import edu.uconn.vstlf.data.Calendar;

public class MonthIndexBlock extends InputBlock {

	private double[] mid_ = new double[12];

	public MonthIndexBlock(Date t, Calendar cal) {
		mid_[cal.getMonth(t)] = 1;	
	}
	
	@Override
	public Vector<InputBlock> deriveInputBlocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getInput() {
		return mid_;
	}

}
