package edu.uconn.vstlf.neuro.ekf;

import java.util.Vector;
import java.util.logging.Level;

import org.garret.perst.Index;
import org.garret.perst.Key;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.matrix.Matrix;
import edu.uconn.vstlf.neuro.ANN;
import edu.uconn.vstlf.neuro.WeightObj;

public class EKFANN extends ANN {
	static private double hiddenInput = 1.0;
	private double weightChange = 3.051757812500000e-05;
	
	public void setWeightChange(double wc) { weightChange = wc; }
	public double getWeightChange() { return weightChange; }
	
	// Hold the storage spaces during the computation
	
	
	private int[] layersShp_;
	
	private double[] lastInput_;
	private Matrix   P_;
	
	private boolean isTraining_ = false;
	
	/* contain all neurons in EKFANN.
	 * layer i has layerSize[i] neurons and
	 * each neuron has layerSize[i-1]+1 weights. 
	 * Thus layer i has (layerSize[i]*(layerSize[i-1]+1)) weights
	 */
	private Vector<EKFNeuron[]> neurons_ = new Vector<EKFNeuron[]>();
	
	
	/* 
	 * Constructor of the EKF neural network
	 * takes the size of each layer and create a new EKF network
	 */
	public EKFANN(int [] layersShape) throws Exception
	{
		layersShp_ = layersShape;
		// Add input layer
		neurons_.add(new EKFNeuron[getLayerSize(0)]);
		for (int i = 0; i < getLayerSize(0); ++i)
			neurons_.lastElement()[i] = new EKFNeuron(null, null);
		
		// Add interior layers and output layers
		for (int il = 1; il < getNumLayers(); ++il) {
			TransFunc func = (il == getNumLayers() - 1 ? new Identity() : new Tanh());
			
			neurons_.add(new EKFNeuron[getLayerSize(il)]);
			for (int i = 0; i < getLayerSize(il); ++i) {
				double [] weights = new double[getNeuronWeightSize(il)];
				for (int j = 0; j < getNeuronWeightSize(il); ++j)
					weights[j] = Math.random();
				neurons_.lastElement()[i] = new EKFNeuron(weights, func);
			}
		}
		
		lastInput_ = new double[getLayerSize(0)];
		P_ = Matrix.identityMatrix(getWeights().length);
	}
	
	/* get the weights of the EKF network */
	public double[] getWeights() throws Exception
	{
		int n = getWeightsSize();
		double[] w = new double[n];
		
		int index = 0;
		for (int l = 0; l < getNumLayers()-1; ++l) {
			for (int fromId = 0; fromId < getLayerSize(l) + 1; ++fromId) {
				// get weights from neuron (l, fromId) to every neuron in the next layer
				for (int toId = 0; toId < getLayerSize(l+1); ++toId) {
					EKFNeuron neu = neurons_.get(l+1)[toId];
					w[index] = neu.getWeights()[fromId];
					++index;
				}
			}
		}
		
		/*
		int index = 0;
		for (int l = 1; l < getNumLayers(); ++l) {
			EKFNeuron[] neurons = neurons_.elementAt(l);
			for (int i = 0; i < neurons.length; ++i) {
				double[] srcw = neurons[i].getWeights();
				System.arraycopy(srcw, 0, w, index, srcw.length);
				index += srcw.length;
			}
		}*/
		if (index != getWeightsSize())
			throw new Exception("Error while getting weights");
		return w;
	}
	
	/* set the weight from neuron [layer-1, fromIndex] to neuron[layer, neuronIndex] 
	 */
	public void setWeight(int layer, int neuronIndex, int fromIndex, double weight)
	{
		EKFNeuron[] neurons = neurons_.elementAt(layer);
		double[] weights = neurons[neuronIndex].getWeights();
		weights[fromIndex] = weight;
	}
	
	/* set weights of the EKF network */
	public void setWeights(double[] weights) throws Exception
	{
		if (weights.length != getWeightsSize())
			throw new Exception("the weights to be set is too large/small");
		
		int index = 0;
		for (int l = 0; l < getNumLayers()-1; ++l) {
			for (int fromId = 0; fromId < getLayerSize(l) + 1; ++fromId) {
				// Set weights from neuron (l, id) to every neuron in the next layer
				for (int toId = 0; toId < getLayerSize(l+1); ++toId) {
					EKFNeuron neu = neurons_.get(l+1)[toId];
					neu.getWeights()[fromId] = weights[index];
					++index;
				}
			}
		}
		
		if (index != getWeightsSize())
			throw new Exception("Error while setting weights");
	}
	
	/* get the number of layers */
	public int getNumLayers() { return layersShp_.length; }
	/* get the size of layer 'layerIndex'*/
	public int getLayerSize(int layerIndex) { return layersShp_[layerIndex]; }
	
	/* get the number of weights in EKF network */
	public int getWeightsSize()
	{
		int n = 0;
		for (int l = 1; l < getNumLayers(); ++l)
			n += getLayerSize(l)*getNeuronWeightSize(l);
		return n;
	}
	
	/* get the number of weights to a neuron in layer 'layerIndex' */
	private int getNeuronWeightSize(int layerIndex)
	{
		return getLayerSize(layerIndex-1)+1;
	}
	
	/* run the neuron network with inputs and produces outputs */
	public double[] execute(double[] inputs) throws Exception
	{
		setInput(inputs);
		forwardPropagate();
		return getOutput();
	}
	
	/* set the inputs to EKF network */
	public void setInput(double[] input) throws Exception
	{
		System.arraycopy(input, 0, lastInput_, 0, input.length);
		if (input.length != getLayerSize(0))
			throw new Exception("Input is not compatible with the EKF neural network");
		
		EKFNeuron[] inputLayer = neurons_.firstElement();
		for (int i = 0; i < input.length; ++i) {
			inputLayer[i].setWeightedSum(input[i]);
			inputLayer[i].setOutput(input[i]);
		}
	}
	
	/* get the outputs to the EKF network */
	public double [] getOutput() {
		double [] output = new double[getLayerSize(getNumLayers()-1)];
		EKFNeuron[] outputLayer = neurons_.lastElement();
		for (int i = 0; i < output.length; ++i)
			output[i] = outputLayer[i].getOutput();
		
		return output;
	}
	
	/* forward propagate starting from layer 1*/
	public void forwardPropagate() throws Exception
	{
		forwardPropagate(1, getNumLayers()-1);
	}
	
	/* forward propagate starting from the 'startLayer' to 'endLayer' */
	public void forwardPropagate(int startLayer, int endLayer) throws Exception
	{
		if (startLayer < 1 || startLayer >= getNumLayers())
			throw new Exception("EKFANN cannot start forward propagation from layer " + startLayer);
		
		for (int li = startLayer; li <= endLayer; ++li) {
			EKFNeuron[] curLayer = neurons_.elementAt(li);
			EKFNeuron[] prevLayer = neurons_.elementAt(li - 1);
			// Get inputs from the previous layer
			double[] prevInput = new double[getLayerSize(li-1)+1];
			for (int pi = 0; pi < getLayerSize(li-1); ++pi)
				prevInput[pi] = prevLayer[pi].getOutput();
			prevInput[getLayerSize(li-1)] = hiddenInput;
			
			// Forward propagate
			for (int ni = 0; ni < getLayerSize(li); ++ni) {
				curLayer[ni].forwardPropagate(prevInput);
			}
		}
		
	}
	
	/*
	 * Forward propagate the weight change from neuron [toNeuronLayer-1, fromNeuronIndex]
	 * to neuron [toNeuronLayer, toNeuronIndex]
	 */
	public double[] fowardPropagateWeightChange(int toNeuronLayer, int fromNeuronIndex, int toNeuronIndex, double weightChange) throws Exception
	{
		if (toNeuronLayer < 1 || toNeuronLayer >= getNumLayers())
			throw new Exception("Cannot change the weight in neuron at layer " + toNeuronLayer 
					+". EKF network has only " + getNumLayers() + " layers");
		
		EKFNeuron[] prevLayer = neurons_.elementAt(toNeuronLayer-1);
		EKFNeuron[] toLayer = neurons_.elementAt(toNeuronLayer);
		EKFNeuron toNeuron = toLayer[toNeuronIndex];
		
		// Save changed data
		Vector<SavedNeuronData> savedata = new Vector<SavedNeuronData>();
		savedata.add(new SavedNeuronData(toNeuron));
		for (int l = toNeuronLayer+1; l < getNumLayers(); ++l) {
			EKFNeuron[] neurons = neurons_.elementAt(l);
			for (int n = 0; n < neurons.length; ++n)
				savedata.add(new SavedNeuronData(neurons[n]));
		}
		
		// incrementally compute the weighted sum and output after weight adjustment
		double oldWeight = toNeuron.getWeights()[fromNeuronIndex];
		toNeuron.getWeights()[fromNeuronIndex] += weightChange;
		double[] prevInput = new double[getLayerSize(toNeuronLayer-1)+1];
		for (int pi = 0; pi < getLayerSize(toNeuronLayer-1); ++pi)
			prevInput[pi] = prevLayer[pi].getOutput();
		prevInput[getLayerSize(toNeuronLayer-1)] = hiddenInput;
		// Forward propagate toNeuron
		toNeuron.forwardPropagate(prevInput);
				
		// incrementally compute the weighted sum and output in the next layer
		int nextLayer = toNeuronLayer;
		/*if (nextLayer < getNumLayers()) {
			double inputChange = toNeuron.getOutput() - savedata.firstElement().output;
			EKFNeuron[] neurons = neurons_.elementAt(nextLayer);
			for (int i = 0; i < neurons.length; ++i) {
				EKFNeuron neu = neurons[i];
				double weight = neu.getWeights()[toNeuronIndex];
				neu.setWeightedSum(neu.getWeightedSum() + inputChange*weight);
				neu.setOutput(neu.computeOutput(neu.getWeightedSum()));
			}
		}*/
		
		// forward propagate
		if (nextLayer + 1 < getNumLayers())
			forwardPropagate(nextLayer + 1, getNumLayers()-1);
		
		double[] changeOutput = getOutput();
		// restore the outputs;
		for (int i = 0; i < savedata.size(); ++i) {
			SavedNeuronData snd = savedata.elementAt(i);
			EKFNeuron n = snd.neuron;
			n.setWeightedSum(snd.weightedSum);
			n.setOutput(snd.output);
		}
		
		toNeuron.getWeights()[fromNeuronIndex] = oldWeight;
		return changeOutput;
	}
	
	boolean spaceReserved_ = false;
	Matrix H_t1, S_t1, S_temp, K_t1, KHMult, RK_trans_mult, KRK_trans, P_temp_mult, P_temp;
	
	/* reserve spaces for backward propagation */
	public void ReserveBPSpace()
	{
		int wn = getWeightsSize();
		int outn = neurons_.lastElement().length;
		if (!spaceReserved_) {
			H_t1 = new Matrix(outn, getWeightsSize());
			K_t1 = new Matrix(wn, outn);
			S_t1 = new Matrix(outn, outn);
			S_temp = new Matrix(wn, outn);
			KHMult = new Matrix(wn, wn);
			RK_trans_mult = new Matrix(outn, wn);
			KRK_trans = new Matrix(wn, wn);
			P_temp_mult = new Matrix(wn, wn);
			P_temp = new Matrix(wn, wn);
			spaceReserved_ = true;
		}
	}
	
	/* release spaces for backward propagation */
	public void ReleaseBPSpace()
	{
		H_t1 = null;
		K_t1 = null;
		S_t1 = null;
		S_temp = null;
		KHMult = null;
		RK_trans_mult = null;
		KRK_trans = null;
		P_temp_mult = null;
		P_temp = null;
		spaceReserved_ = false;
	}
	
	// for matlab test case
	// since we need to modify the weights in back propagation, it need
	// to be an array to references
	public void backwardPropTest(double[] inputs, double[] outputs, Double[] weights, Matrix P, Matrix Q, Matrix R) throws Exception
	{
		double[] w = new double[weights.length];
		for (int i = 0; i < w.length; ++i) w[i] = weights[i];
		backwardPropagate(inputs, outputs, w, P, Q, R);
		for (int i = 0; i < w.length; ++i) weights[i] = w[i];
	}
	
	
	/* Backward propagation, refer to 'doc/hekf.tex' file the "training iteration"
	 * section for details. The comments shows which equations each step corresponds to.
	 */
	public void backwardPropagate(double[] inputs, double[] outputs, double[] weights, Matrix P, Matrix Q, Matrix R) throws Exception
	{
		int wn = getWeightsSize();
		if (wn != weights.length) throw new Exception("Back propagation failed: the input weight array has a different size");
		int outn = neurons_.lastElement().length;
		
		// Test if elements in P is too small
		/*for (int i = 0; i < P.getRow(); ++i)
			for (int j = 0; j < P.getCol(); ++j)
				if (P.getVal(i, j) != 0.0 && Math.abs(P.getVal(i, j)) < 10e-20)
					throw new Exception(P.getVal(i, j) + "!!!!!!!!!P is too small!!!!!!!!!!!!!!");
					*/
		if (!isTraining_) ReserveBPSpace();
		
		setWeights(weights);
		
		/* Back propagate from time t to t+1.
		 * In the following names t1 means at time t+1, for example w_t1_t refers to
		 * w(t+1|t) in the equations in 'doc/hekf.tex'
		 */
		
		// equation (5)
		double[] w_t1_t = weights;   	
		
		// equation (6)
		Matrix P_t_t = P;				
		Matrix P_t1_t = P_t_t;			
		Matrix.add(P_t1_t, Q);			
		
		// equation (7)
		double[] z_t1_t = execute(inputs);
		
		// get jacobian matrix
		jacobian(H_t1);
		
		// equation (8) compute S(t+1)
		Matrix.multiply(false, true, P_t1_t, H_t1, S_temp);
		Matrix.multiply(false, false, H_t1, S_temp, S_t1);
		Matrix.add(S_t1, R);
		
		// equation(9) compute K(t+1) = P(t+1|t)*H(t+1)'*S(t+1)^-1
		// compute X = H(t+1)'*S(t+1)^-1 first, it is the same to solve 
		// S(t+1)'X'=H(t+1)
		Matrix X_trans = new Matrix(H_t1.getRow(), H_t1.getCol()); // Matrix X'
		// Solve S(t+1)'X'=H(t+1), get X' in X_trans
		Matrix.solveLinearEqu(true, Matrix.copy(S_t1), Matrix.copy(H_t1), X_trans);
		Matrix.multiply(false, true, P_t1_t, X_trans, K_t1);
		
		// equation(10) Compute w(t+1|t+1)
		double [] uz = new double[outn];
		for (int i = 0; i < outn; ++i)
			uz[i] = outputs[i] - z_t1_t[i];
		double [] w_t1_t1 = new double[wn];
		Matrix.multiply(K_t1, uz, w_t1_t1);
		for (int i = 0; i < wn; ++i)
			w_t1_t1[i] += w_t1_t[i];
		
		// equation(11)
		Matrix.multiply(false, false, K_t1, H_t1, KHMult);
		// Compute I-K(t)*H(t)
		for (int i = 0; i < wn; ++i)
			for (int j = 0; j < wn; ++j)
				KHMult.setVal(i, j, (i == j ? 1.0 - KHMult.getVal(i, j) : -KHMult.getVal(i,j)));
		Matrix I_minus_KHMult = KHMult;
		
		// Compute K(t+1)*R(t+1)*K(t+1)'
		Matrix.multiply(false, true, R, K_t1, RK_trans_mult);
		Matrix.multiply(false, false, K_t1, RK_trans_mult, KRK_trans);
		
		// Compute (I - K(t+1)*H(t+1))*P(t+1|t)*(I-K(t+1)*H(t+1))'
		Matrix.multiply(false, true, P_t1_t, I_minus_KHMult, P_temp_mult);
		Matrix.multiply(false, false, I_minus_KHMult, P_temp_mult, P_temp);
		
		Matrix.add(P_temp, KRK_trans);
		
		// equation(12)
		for (int i = 0; i < wn; ++i)
			for (int j = 0; j < wn; ++j)
				P.setVal(i, j, (P_temp.getVal(i, j) + P_temp.getVal(j, i))/2.0);
		
		if (!isTraining_) ReleaseBPSpace();
		// copy back weights
		System.arraycopy(w_t1_t1, 0, weights, 0, wn);
	}
	
	public Matrix jacobian(Matrix H_t) throws Exception
	{
		double[] refout = getOutput();
		int hCol = 0;
		for (int l = 1; l < getNumLayers(); ++l){
			int fromLayer = l-1;
			for (int fromNeuronIndex = 0; fromNeuronIndex < getLayerSize(fromLayer)+1; ++fromNeuronIndex) {
				for (int toNeuronIndex = 0; toNeuronIndex < getLayerSize(l); ++toNeuronIndex) {
					// Compute a column of H(t)					
					double [] pout = fowardPropagateWeightChange(l, fromNeuronIndex, toNeuronIndex, weightChange);
					
					for (int k = 0; k < pout.length; ++k)
						H_t.setVal(k, hCol, (pout[k]-refout[k])/weightChange);
					++hCol;
				}
			}
		}
		return H_t;
	}
	
	public void setP(Matrix P)
	{	P_ = P;  }
	
	public Matrix getP()
	{
		return P_;
	}
	
	public Matrix getQ()
	{
		int wn = getWeightsSize();
		Matrix Q = Matrix.identityMatrix(wn);
		Matrix.multiply(0.000005, Q);
		return Q;
	}
	
	public Matrix getR()
	{
		int outn = neurons_.lastElement().length;
		Matrix R = Matrix.identityMatrix(outn);
		Matrix.multiply(0.00001, R);
		return R;
	}
	
	/*
	 * Train EKF ANN with inputs and target outputs for 'iterations' iterations
	 */
	public void train(Series[] in, Series[] tg, int iterations) throws Exception
	{
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "Training EKFANN for " + iterations + " iterations"));
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		double[] weights = getWeights();
		Matrix P = Matrix.identityMatrix(weights.length);
		isTraining_ = true;
		ReserveBPSpace();
		for (int it = 0; it < iterations; ++it) {
			int tenPercent = 0;
			
			Matrix Q = getQ();
			Matrix R = getR();
			for(int i = 0;i<in.length;i++){
				backwardPropagate(in[i].array(), tg[i].array(), weights, P, Q, R);
				int p = (int)((double)i/(double)in.length*10);
				if ( p != tenPercent) {
					MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "complete back propagation of " + p*10 + "% (" + i + "/" + in.length + ")inputs"));
					tenPercent = p;
				}
			}
			MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "Iteration " + it + " done. 100% (" + in.length + "/" + in.length + ")"));
		}
		ReleaseBPSpace();
		isTraining_ = false;

		MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "\tDone."));
	}

	/* exectution function with Series as inputs */
	@Override
	public Series execute(Series in) throws Exception {
		return new Series(execute(in.array(false)),false);
	}

	private WeightObj[] getWeightObjs() {
		Vector<WeightObj> currV = new Vector<WeightObj>();
		for (int l = 1; l < neurons_.size(); ++l) {
			EKFNeuron[] ns = neurons_.elementAt(l);
			for (int nid = 0; nid < ns.length; ++nid) {
				EKFNeuron neu = ns[nid];
				double[] weights = neu.getWeights();
				for (int sid = 0; sid < weights.length; ++sid)
					currV.add(new WeightObj(l, nid, sid, weights[sid]));
			}
		}
		
		WeightObj[] curr = new WeightObj[currV.size()];
		for(int i = 0; i<curr.length;i++){
			curr[i] = currV.elementAt(i);
		}
		return curr;
	}

	@Override
	public int[] getLayerSize() {
		return layersShp_;
	}

	/* a single update to the EKF network */
	@Override
	public void update(Series trg) throws Exception {
		double[] outputs = trg.array(false);
		double[] weights = getWeights();
		
		Matrix Q = getQ();
		Matrix R = getR();
		
		backwardPropagate(lastInput_, outputs, weights, P_, Q, R);
		setWeights(weights);
	}

	/*
	 * Save the network to Perst database(non-Javadoc)
	 * @see edu.uconn.vstlf.neuro.ANN#save(java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void save(String file, int id) throws Exception {
		WeightObj[] curr = getWeightObjs();
		double[] Pary = new double[P_.getRow()*P_.getCol()];
		for (int i = 0; i < P_.getRow(); ++i)
			for (int j = 0; j < P_.getCol(); ++j)
				Pary[i*P_.getRow()+j] = P_.getVal(i, j);
		EKFWeightSet content = new EKFWeightSet(id,getLayerSize(),curr, Pary);
		
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<EKFWeightSet> sets = (Index<EKFWeightSet>)db.getRoot();
		if (sets == null) { 
            sets = db.createIndex(int.class, true);
            db.setRoot(sets);
        }
		if(sets.get(new Key(id))!=null) sets.remove(new Key(id));
		sets.put(new Key(id),content);
		db.commit();
		db.close();		
	}
	
	/*
	 * Load the network from a perst database
	 */
	@SuppressWarnings("unchecked")
	public
	static EKFANN load(String file, int id)throws Exception{
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<EKFWeightSet> sets = (Index<EKFWeightSet>)db.getRoot();
		if (sets == null) { 
            throw new Exception("There are no ANNs stored in the file '" + file +"'.");
        }
		EKFWeightSet content = sets.get(new Key(id));
		if(content==null){
			throw new Exception("ANN #"+id+"is not stored in the file '" + file +"'.");
		}
		EKFANN ann = new EKFANN(content._lyrSz);
		WeightObj[] curr = content.getWeights();
		for (int i = 0; i < curr.length; ++i) {
			WeightObj wo = curr[i];
			ann.setWeight(wo.lid(), wo.nid(), wo.cid(), wo.val());
		}
		
		double[] Pary = content.getP();
		Matrix P = new Matrix(curr.length, curr.length);

		for (int i = 0; i < curr.length; ++i)
			for (int j = 0; j < curr.length; ++j)
				P.setVal(i, j, Pary[i*curr.length+j]);
		ann.setP(P);
		return ann;
	}
}

class SavedNeuronData
{
	SavedNeuronData(EKFNeuron neu)
	{
		neuron = neu;
		weightedSum = neu.getWeightedSum();
		output = neu.getOutput();
	}
	
	public EKFNeuron neuron;
	public double weightedSum, output;
}

abstract class TransFunc
{
	public abstract double compute(double v);
}

class Tanh extends TransFunc
{
	public double compute(double v) { return Math.tanh(v); }
}

class Identity extends TransFunc
{
	public double compute(double v) { return v; }
}

class EKFNeuron
{
	private double weightedSum_, output_;
	
	private double [] weights_;
	private TransFunc func_;
	public EKFNeuron(double[] weights, TransFunc func)
	{
		weights_ = weights;
		func_ = func;
	}
	
	double forwardPropagate(double[] inputs) throws Exception
	{
		if (inputs.length != weights_.length)
			throw new Exception("In EKFNeuron: too many/few inputs");
		
		weightedSum_ = computeWeightedSum(inputs);
		output_ = computeOutput(weightedSum_);
		return output_;
	}

	double[] getWeights() { return weights_; }
	
	double getWeightedSum() { return weightedSum_; }
	void setWeightedSum(double ws) { weightedSum_ = ws; }
	double computeWeightedSum(double[] inputs)
	{
		double weightedSum = 0.0;
		for (int i = 0; i < inputs.length; ++i) {
			weightedSum += inputs[i]*weights_[i];
		}
		return weightedSum;
	}
	
	double getOutput() { return output_; }
	void setOutput(double out) { output_ = out; }
	double computeOutput(double weightedSum) 
	{ return func_.compute(weightedSum); }
	
}