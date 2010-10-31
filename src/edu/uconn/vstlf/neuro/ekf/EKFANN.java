package edu.uconn.vstlf.neuro.ekf;

import java.util.Vector;

public class EKFANN {
	static private double hiddenInput = 1.0;
	
	private int[] layersShp_;
	
	/* contain all neurons in EKFANN.
	 * layer i has layerSize[i] neurons and
	 * each neuron has layerSize[i-1]+1 weights. 
	 * Thus layer i has (layerSize[i]*(layerSize[i-1]+1)) weights
	 */
	private Vector<EKFNeuron[]> neurons_;
	
	public EKFANN(int [] layersShape)
	{
		layersShp_ = layersShape;
		// Add input layer
		neurons_.add(new EKFNeuron[getLayerSize(0)]);
		for (int i = 0; i < getLayerSize(0); ++i)
			neurons_.lastElement()[i] = new EKFNeuron(null, null);
		
		// Add interior layers and output layers
		for (int il = 1; il < getNumLayers(); ++il) {
			double [] weights = new double[getNeuronWeightSize(il)];
			for (int j = 0; j < getNeuronWeightSize(il); ++j)
				weights[j] = Math.random();
			TransFunc func = (il == getNumLayers() - 1 ? new Identity() : new Tanh());
			
			neurons_.add(new EKFNeuron[getLayerSize(il)]);
			for (int i = 0; i < getLayerSize(il); ++i)
				neurons_.lastElement()[i] = new EKFNeuron(weights, func);
		}
	}
	
	public int getNumLayers() { return layersShp_.length; }
	public int getLayerSize(int layerIndex) { return layersShp_[layerIndex]; }
	
	private int getNeuronWeightSize(int layerIndex)
	{
		return getLayerSize(layerIndex-1)+1;
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
	
	public void fowardPropagateWeightChange(int toNeuronLayer, int fromNeuronIndex, int toNeuronIndex, double weightChange) throws Exception
	{
		if (toNeuronLayer < 1 || toNeuronLayer >= getNumLayers())
			throw new Exception("Cannot change the weight in neuron at layer " + toNeuronLayer 
					+". EKF network has only " + getNumLayers() + " layers");
		
		EKFNeuron[] prevLayer = neurons_.elementAt(toNeuronLayer-1);
		EKFNeuron[] toLayer = neurons_.elementAt(toNeuronLayer);
		double fromInput = prevLayer[fromNeuronIndex].getOutput();
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
		forwardPropagate(nextLayer + 1, getNumLayers()-1);
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