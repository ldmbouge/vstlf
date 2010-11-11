package edu.uconn.vstlf.neuro.ekf;

import org.garret.perst.Index;
import org.garret.perst.Key;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.matrix.Matrix;
import edu.uconn.vstlf.neuro.ANNBank;

public class EKFANNBank extends ANNBank {

	EKFANN[] anns_;
	double[][] inputs_;
	Matrix[] P_;
	public EKFANNBank(int[][] lyrSz) throws Exception {
		anns_ = new EKFANN[lyrSz.length];
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
			anns_[id] = load(file, id);
		}	
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
	
	@SuppressWarnings("unchecked")
	public void save(String file) throws Exception{
		for(int id=0;id<anns_.length;id++) {
			EKFANN ann = anns_[id];
			double[] weights = ann.getWeights();
			WeightObj[] currV = new WeightObj[weights.length];
	
			for (int i = 0; i < weights.length; ++i)
				currV[i] = new WeightObj(weights[i]);
	
			int[] lyrSz = new int[ann.getNumLayers()];
			for (int i = 0; i < ann.getNumLayers(); ++i)
				lyrSz[i] = ann.getLayerSize(i);
			WeightSet content = new WeightSet(id,lyrSz,currV);
			Storage db = StorageFactory.getInstance().createStorage();
			db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
			Index<WeightSet> sets = (Index<WeightSet>)db.getRoot();
			if (sets == null) { 
	            sets = db.createIndex(int.class, true);
	            db.setRoot(sets);
	        }
			if(sets.get(new Key(id))!=null) sets.remove(new Key(id));
			sets.put(new Key(id),content);
			db.commit();
			db.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static EKFANN load(String file, int id)throws Exception{
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<WeightSet> sets = (Index<WeightSet>)db.getRoot();
		if (sets == null) { 
            throw new Exception("There are no ANNs stored in the file '" + file +"'.");
        }
		WeightSet content = sets.get(new Key(id));
		if(content==null){
			throw new Exception("ANN #"+id+"is not stored in the file '" + file +"'.");
		}
		EKFANN ann = new EKFANN(content._lyrSz);
		WeightObj[] curr = content._curr;
		double[] weights = new double[curr.length];
		for (int i = 0; i < weights.length; ++i)
			weights[i] = curr[i].val();
		ann.setWeights(weights);
		return ann;
	}
}


class WeightObj extends Persistent{
	double _val;
	public WeightObj() {}
	WeightObj(double val){
		_val = val;
	}

	double val(){return _val;}
	public boolean equals(Object o) {
		return super.equals(o);
	}
	public int hashCode() { return 7;}
}

class WeightSet extends Persistent{
	long _id;
	int[] _lyrSz;
	WeightObj[] _curr;
	public WeightSet() {
		_id = -1;
		_lyrSz = null;
		_curr = null;
	}
	WeightSet(int id, int[] lyrSz, WeightObj[] curr){
		_id = id; _curr = curr; _lyrSz = lyrSz;
	}
	public boolean equals(Object o) {
		return super.equals(o);
	}
	public int hashCode() { return 7;}
}