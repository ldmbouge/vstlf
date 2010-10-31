package edu.uconn.vstlf.neuro.ekf;

public class EKFANN {
	static private double hiddenInput = 1.0;
	
	private int[] layersShp_;
	
	/* weights_ contains all weights in EKFANN.
	 * layer i has layerSize[i] neurons and
	 * each neuron has layerSize[i-1]+1 weights. 
	 * Thus layer i has (layerSize[i]*(layerSize[i-1]+1)) weights
	 */
	private double[] weights_;
	
	// contains the weighted sums saved during the full propagation
	private double[] weightedSums_;
	private double[] inputs_;
	
	public EKFANN(int [] layersShape)
	{
		layersShp_ = layersShape;
		int n = 0;
		for (int il = 1; il < getNumLayers(); ++il)
			n += getLayerWeightSize(il);
		weights_ = new double[n];
		// Give some random initial weights
		for (int i = 0; i < n; ++i)
			weights_[i] = Math.random();
		
		int sumN = 0;
		for (int il = 0; il < getNumLayers(); ++il)
			sumN += getLayerSize(il);
		weightedSums_ = new double[sumN];
		inputs_ = new double[sumN];
	}
	
	public int getNumLayers() { return layersShp_.length; }
	public int getLayerSize(int layerIndex) { return layersShp_[layerIndex]; }
	
	private int getLayerWeightSize(int layerIndex)
	{
		return getLayerSize(layerIndex)*(getLayerSize(layerIndex-1)+1);
	}
	
	private int getNeuronWeightSize(int layerIndex)
	{
		return getLayerSize(layerIndex-1)+1;
	}
	
	private int getNeuronWeightIndex(int layerIndex, int neuronIndex)
	{
		int startWeightIndex = 0;
		// Skip the weights of previous layers
		for (int i = 1; i < layerIndex; ++i)
			startWeightIndex += getLayerWeightSize(i);
		// Skip the weights of previous neurons in this layer
		for (int i = 0; i < neuronIndex; ++i)
			startWeightIndex += getNeuronWeightSize(layerIndex);
		return startWeightIndex;
	}
	
	public double [] getNeuronWeights(int layerIndex, int neuronIndex)
	{
		int startWeightIndex = getNeuronWeightIndex(layerIndex, neuronIndex);
		double [] weights = new double[getNeuronWeightSize(layerIndex)];
		for (int i = 0; i < weights.length; ++i)
			weights[i] = weights_[startWeightIndex+i];
		
		return weights;
	}
	
	public void setNeuronWeights(int layerIndex, int neuronIndex, double [] weights)
	{
		int startWeightIndex = getNeuronWeightIndex(layerIndex, neuronIndex);
		for (int i = 0; i < getNeuronWeightSize(layerIndex); ++i)
			weights_[startWeightIndex+i] = weights[i];
	}
	
	public void setWeights(double [] weights) throws Exception
	{
		if (weights_.length != weights.length)
			throw new Exception("Cannot set weights of EKFANN. Input weight vector size is not the same as EKFANN's weight vector");
		weights_ = weights;
	}
	
	public double[] forwardPropagate(double[] input) throws Exception
	{
		if (input.length != getLayerSize(0))
			throw new Exception("Input is not compatible with the EKF neural network");
		
		for (int i = 0; i < input.length; ++i) {
			weightedSums_[i] = input[i];
			inputs_[i] = input[i];
		}
		
		int weightIndex = 0;
		int inputIndex = 0;
		int sumI = input.length;
		for (int li = 1; li < getNumLayers(); ++li) {
			int pli = li-1;
			int prevInputSize = getLayerSize(pli);
			for (int ni = 0; ni < getLayerSize(li); ++ni, ++sumI, weightIndex += getNeuronWeightSize(li)) {
				// Calculate the weighted sum at this neuron
				double wsum = 0.0;
				for (int ini = 0; ini < prevInputSize; ++ini)
					wsum += inputs_[inputIndex+ini]*weights_[weightIndex+ini];
				// Add the hidden input
				wsum += hiddenInput*weights_[weightIndex+prevInputSize+1];
				
				weightedSums_[sumI] = wsum;
				inputs_[sumI] = Math.tanh(wsum);
			}
			inputIndex += prevInputSize;
		}
		
		double [] output = new double[getLayerSize(getNumLayers()-1)];
		int outI = 0;
		for (int i = 0; i < getNumLayers() - 1; ++i)
			outI += getLayerSize(i);
		for (int i = 0; i < output.length; ++i)
			output[i] = inputs_[outI+i];
		
		return output;
	}
	
	
}
