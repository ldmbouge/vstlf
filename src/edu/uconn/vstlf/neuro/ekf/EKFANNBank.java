package edu.uconn.vstlf.neuro.ekf;

import org.garret.perst.Index;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.matrix.Matrix;
import edu.uconn.vstlf.neuro.ANNBank;
import edu.uconn.vstlf.neuro.WeightSet;

public class EKFANNBank extends ANNBank {

	EKFANN[] anns_;
	double[][] inputs_;
	Matrix[] P_;
	
	private void init(int[][] lyrSz) throws Exception
	{
		inputs_ = new double[lyrSz.length][];
		P_ = new Matrix[lyrSz.length];
		for(int i = 0;i<lyrSz.length;i++) {
			anns_[i] = new EKFANN(lyrSz[i]);
			// Initialize the array storing the input in the last execution for back propagation
			inputs_[i] = new double[lyrSz[i][0]];
			// Initialize the P matrix for back propagation
			int wn = anns_[i].getWeights().length;
			P_[i]  = Matrix.identityMatrix(wn);
		}
	}
	public EKFANNBank(int[][] lyrSz) throws Exception {
		anns_ = new EKFANN[lyrSz.length];
		init(lyrSz);
	}

	public EKFANNBank(String file) throws Exception {
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		@SuppressWarnings("unchecked")
		Index<WeightSet> sets = (Index<WeightSet>)db.getRoot();
		if (sets == null) { 
            throw new Exception("There are no ANNs stored in the file '" + file +"'.");
        }
		int nbnn = sets.size();
		db.close();
		anns_ = new EKFANN[nbnn];
		for(int id = 0;id<nbnn;id++){
			anns_[id] = EKFANN.load(file, id);
		}	
		int [][] lyrsz = new int[anns_.length][];
		for (int i = 0; i < anns_.length; ++i)
			lyrsz[i] = anns_[i].getLayerSize();
		init(lyrsz);
	}

	@Override
	public Series[] execute(Series[] in) throws Exception {
		Series[] out = new Series[anns_.length];
		for(int i = 0;i<out.length;i++) {
			inputs_[i] = in[i].array();
			out[i] = new Series(anns_[i].execute(inputs_[i]));
		}
		return out;
	}

	@Override
	public void update(Series[] tg) throws Exception {
		for (int i = 0; i < anns_.length; ++i) {
			double[] inputs = inputs_[i];
			double[] outputs = tg[i].array();
			double[] weights = anns_[i].getWeights();
			anns_[i].backwardPropagate(inputs, outputs, weights, P_[i]);
			anns_[i].setWeights(weights);
		}
	}

	public void train(Series[][] in, Series[][] tg, int iterations)throws Exception{
		Series[][] inf = new Series[in[0].length][in.length];
		Series[][] tgf = new Series[tg[0].length][tg.length];
		for(int i = 0;i<in.length;i++){
			for(int j = 0;j<inf.length;j++){
				inf[j][i] = in[i][j];
				tgf[j][i] = tg[i][j];
			}
			
		}
		for(int i = 0;i<anns_.length;i++)
			anns_[i].train(inf[i], tgf[i], iterations);
	}
	
	public void save(String file) throws Exception{
		for(int id=0;id<anns_.length;id++) anns_[id].save(file, id);
	}
	
}