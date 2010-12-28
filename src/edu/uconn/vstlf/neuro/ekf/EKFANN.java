package edu.uconn.vstlf.neuro.ekf;

import java.util.Vector;
import java.util.logging.Level;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.matrix.Matrix;

public class EKFANN {
	static private double hiddenInput = 1.0;
	static public double weightChange = 1.27009514017118e-13;
	
	private int[] layersShp_;
	
	/* contain all neurons in EKFANN.
	 * layer i has layerSize[i] neurons and
	 * each neuron has layerSize[i-1]+1 weights. 
	 * Thus layer i has (layerSize[i]*(layerSize[i-1]+1)) weights
	 */
	private Vector<EKFNeuron[]> neurons_ = new Vector<EKFNeuron[]>();
	
	public EKFANN(int [] layersShape)
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
	}
	
	public double[] getWeights() throws Exception
	{
		int n = getWeightsSize();
		double[] w = new double[n];
		
		int index = 0;
		for (int l = 0; l < getNumLayers()-1; ++l) {
			for (int fromId = 0; fromId < getLayerSize(l) + 1; ++fromId) {
				// get weights from neuron (l, id) to every neuron in the next layer
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
		/*
		for (int l = 1; l < getNumLayers(); ++l) {
			EKFNeuron[] neurons = neurons_.elementAt(l);
			int wlen = getNeuronWeightSize(l);
			for (int i = 0; i < neurons.length; ++i) {
				EKFNeuron neu = neurons[i];
				System.arraycopy(weights, index, neu.getWeights(), 0, wlen);
				index += wlen;
			}
		}*/
		
		if (index != getWeightsSize())
			throw new Exception("Error while setting weights");
	}
	
	public int getNumLayers() { return layersShp_.length; }
	public int getLayerSize(int layerIndex) { return layersShp_[layerIndex]; }
	
	private int getWeightsSize()
	{
		int n = 0;
		for (int l = 1; l < getNumLayers(); ++l)
			n += getLayerSize(l)*getNeuronWeightSize(l);
		return n;
	}
	
	private int getNeuronWeightSize(int layerIndex)
	{
		return getLayerSize(layerIndex-1)+1;
	}
	
	public double[] execute(double[] inputs) throws Exception
	{
		setInput(inputs);
		forwardPropagate();
		return getOutput();
	}
	
	public void setInput(double[] input) throws Exception
	{
		if (input.length != getLayerSize(0))
			throw new Exception("Input is not compatible with the EKF neural network");
		
		EKFNeuron[] inputLayer = neurons_.firstElement();
		for (int i = 0; i < input.length; ++i) {
			inputLayer[i].setWeightedSum(input[i]);
			inputLayer[i].setOutput(input[i]);
		}
	}
	
	public double [] getOutput() {
		double [] output = new double[getLayerSize(getNumLayers()-1)];
		EKFNeuron[] outputLayer = neurons_.lastElement();
		for (int i = 0; i < output.length; ++i)
			output[i] = outputLayer[i].getOutput();
		
		return output;
	}
	
	public void forwardPropagate() throws Exception
	{
		forwardPropagate(1, getNumLayers()-1);
	}
	
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
	
	public double[] fowardPropagateWeightChange(int toNeuronLayer, int fromNeuronIndex, int toNeuronIndex, double weightChange) throws Exception
	{
		if (toNeuronLayer < 1 || toNeuronLayer >= getNumLayers())
			throw new Exception("Cannot change the weight in neuron at layer " + toNeuronLayer 
					+". EKF network has only " + getNumLayers() + " layers");
		
		EKFNeuron[] prevLayer = neurons_.elementAt(toNeuronLayer-1);
		EKFNeuron[] toLayer = neurons_.elementAt(toNeuronLayer);
		double fromInput = (fromNeuronIndex == prevLayer.length ? 
				hiddenInput : prevLayer[fromNeuronIndex].getOutput());
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
		toNeuron.setWeightedSum(toNeuron.getWeightedSum() + weightChange*fromInput);
		toNeuron.setOutput(toNeuron.computeOutput(toNeuron.getWeightedSum()));
		
		// incrementally compute the weighted sum and output in the next layer
		int nextLayer = toNeuronLayer + 1;
		if (nextLayer < getNumLayers()) {
			double inputChange = toNeuron.getOutput() - savedata.firstElement().output;
			EKFNeuron[] neurons = neurons_.elementAt(nextLayer);
			for (int i = 0; i < neurons.length; ++i) {
				EKFNeuron neu = neurons[i];
				double weight = neu.getWeights()[toNeuronIndex];
				neu.setWeightedSum(neu.getWeightedSum() + inputChange*weight);
				neu.setOutput(neu.computeOutput(neu.getWeightedSum()));
			}
		}
		
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
		
		return changeOutput;
	}
	
	public void backwardPropagate(double[] inputs, double[] outputs, double[] weights, Matrix P) throws Exception
	{
		int wn = getWeightsSize();
		int outn = neurons_.lastElement().length;
		
		Matrix 
			Q = Matrix.identityMatrix(wn), 
			R = Matrix.identityMatrix(outn);
		
		Matrix.multiply(0.000005, Q);
		Matrix.multiply(0.00001, R);
		
		setWeights(weights);
		double[] w_t_t1 = weights;
		Matrix P_t1_t1 = P;
		Matrix P_t_t1 = Matrix.copy(P_t1_t1, true);
		Matrix.add(P_t_t1, Q);
		// forward propagation;
		double[] z_t_t1 = execute(inputs);
		// get jacobian matrix
		Matrix H_t = jacobian();
		
		// compute S(t)
		Matrix S_t = new Matrix(outn, outn);
		Matrix S_temp = new Matrix(P_t_t1.getRow(), H_t.getRow());
		Matrix.multiply(false, true, P_t_t1, H_t, S_temp);
		Matrix.multiply(false, false, H_t, S_temp, S_t);
		Matrix.add(S_t, R);
		
		// compute K(t)
		Matrix S_t_inv = new Matrix(outn, outn);
		Matrix.inverse(Matrix.copy(S_t, true), S_t_inv);
		Matrix K_t = new Matrix(wn, outn);
		Matrix.multiply(false, false, Matrix.copy(S_temp, true), S_t_inv, K_t);
		
		// Compute w(t|t)
		double [] uz = new double[outn];
		for (int i = 0; i < outn; ++i)
			uz[i] = outputs[i] - z_t_t1[i];
		double [] w_t_t = new double[wn];
		Matrix.multiply(K_t, uz, w_t_t);
		for (int i = 0; i < wn; ++i)
			w_t_t[i] += w_t_t1[i];
		
		Matrix KHMult = new Matrix(wn, wn);
		Matrix.multiply(false, false, K_t, Matrix.copy(H_t, false), KHMult);
		// Compute I-K(t)*H(t)
		for (int i = 0; i < wn; ++i)
			for (int j = 0; j < wn; ++j)
				KHMult.setVal(i, j, (i == j ? 1.0 - KHMult.getVal(i, j) : -KHMult.getVal(i,j)));
		Matrix I_minus_KHMult = KHMult;
		
		// Compute P(t|t)
		Matrix RK_trans_mult = new Matrix(outn, wn);
		Matrix.multiply(false, true, R, K_t, RK_trans_mult);
		Matrix KRK_trans = new Matrix(wn, wn);
		Matrix.multiply(false, false, K_t, RK_trans_mult, KRK_trans);
		
		Matrix P_temp_mult = new Matrix(wn, wn);
		Matrix.multiply(false, true, P_t_t1, I_minus_KHMult, P_temp_mult);
		Matrix P_temp = new Matrix(wn, wn);
		Matrix.multiply(false, false, I_minus_KHMult, P_temp_mult, P_temp);
		
		for (int i = 0; i < wn; ++i)
			for (int j = 0; j < wn; ++j)
				P.setVal(i, j, (P_temp.getVal(i, j) + P_temp.getVal(j, i))/2.0);
		// copy back weights
		System.arraycopy(w_t_t, 0, weights, 0, wn);
	}
	
	Matrix jacobian() throws Exception
	{
		double[] refout = getOutput();
		Matrix H_t = new Matrix(neurons_.lastElement().length, getWeightsSize());
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
	
	public void train(Series[] in, Series[] tg, int iterations) throws Exception
	{
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "Training EKFANN for " + iterations + " iterations"));
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		double[] weights = getWeights();
		Matrix P = Matrix.identityMatrix(weights.length);
		for (int it = 0; it < iterations; ++it) {
			for(int i = 0;i<in.length;i++){
				backwardPropagate(in[i].array(), tg[i].array(), weights, P);
				MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "complete back propagation of input " + i));
			}
			MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "Iteration " + it + " done"));
		}
		
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, EKFANNBank.class.getName(), methodName, "\tDone."));
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