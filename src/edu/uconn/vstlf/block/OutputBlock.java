package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;

public abstract class OutputBlock {
	public abstract Series getOutput(Series output) throws Exception;
}
