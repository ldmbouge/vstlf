package edu.uconn.vstlf.prediction;

import java.util.Vector;

import com.web_tomorrow.utils.suntimes.SunTimesException;

public abstract class InputBlock {
	// get the input data
	public abstract double[] getInput();
	public abstract Vector<InputBlock> deriveInputBlocks() throws Exception;
}
