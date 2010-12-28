package edu.uconn.vstlf.neuro;

import edu.uconn.vstlf.data.doubleprecision.Series;

public abstract class ANN {
	public abstract Series execute(Series in)throws Exception;
	public abstract void update(Series trg) throws Exception;
	
	public abstract int[] getLayerSize();
	
	abstract public void save(String file, int id) throws Exception;
}
