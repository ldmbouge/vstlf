package edu.uconn.vstlf.prediction;

import java.util.Date;
import java.util.Vector;

import edu.uconn.vstlf.data.Calendar;

public class WeekIndexBlock extends InputBlock {

	private double[] wdi_ = new double[7];

	public WeekIndexBlock(Date t, Calendar cal) {
		wdi_[cal.getDayOfWeek(t)-1] = 1;
	}
	
	@Override
	public Vector<InputBlock> deriveInputBlocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getInput() {
		// TODO Auto-generated method stub
		return wdi_;
	}

}
