package edu.uconn.vstlf.neuro;

import edu.uconn.vstlf.data.doubleprecision.Series;

public abstract class ANNBank {
	abstract public Series[] execute(Series[] in) throws Exception;
	abstract public void update(Series[] tg) throws Exception;
	
	abstract public void save(String file) throws Exception;
}