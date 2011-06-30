package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;

public abstract class InputBlock {	
	// get the input data
	public abstract Series getInput() throws Exception;
}
