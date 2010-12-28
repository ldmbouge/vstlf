package edu.uconn.vstlf.neuro;

public abstract class ANNFactory {
	abstract public ANNBank buildANNBank(String file) throws Exception;
}
