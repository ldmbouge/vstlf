package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;

public abstract class UpdateBlock {
	public abstract Series getUpdateInput() throws Exception;
}
