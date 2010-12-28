package edu.uconn.vstlf.neuro;

public class SimpANNFac extends ANNFactory {

	@Override
	public ANNBank buildANNBank(String file) throws Exception {
		return new SimpANNBank(file);
	}

}
