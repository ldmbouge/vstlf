package edu.uconn.vstlf.neuro;

import edu.uconn.vstlf.neuro.ekf.EKFANN;
import edu.uconn.vstlf.neuro.ekf.EKFANNBank;

public class EKFANNFac extends ANNFactory {

	@Override
	public ANNBank buildANNBank(String file) throws Exception {
		return new EKFANNBank(file);
	}

	@Override
	public ANN buildANN(String file, int id) throws Exception {
		return EKFANN.load(file, id);
	}

}
