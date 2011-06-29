package edu.uconn.vstlf.prediction;

import java.util.Date;
import java.util.Vector;

import edu.uconn.vstlf.data.Calendar;

public class HourIndexBlock extends InputBlock {

	private double[] hri_ = new double[24];

	public HourIndexBlock(Date t, Calendar cal) {
		hri_[cal.getHour(t)] = 1;
	}
	
	@Override
	public Vector<InputBlock> deriveInputBlocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getInput() {
		// TODO Auto-generated method stub
		return hri_;
	}

}
