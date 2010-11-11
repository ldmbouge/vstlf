package edu.uconn.vstlf.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.uconn.vstlf.matrix.Matrix;
import edu.uconn.vstlf.neuro.ekf.EKFANN;

public class EKFTest {
	@Test public void forwardTest() throws Exception
	{
		String inputCSV = "doc/test/ForwardPropagation/input.csv",
			outputCSV = "doc/test/ForwardPropagation/output.csv",
			weightCSV = "doc/test/ForwardPropagation/weight.csv";
		ReadFromCSV reader = new ReadFromCSV();
		double[][] wr = reader.Read(weightCSV, 572, 1);
		double[] weights = new double[wr.length];
		for (int i = 0; i < weights.length; ++i)
			weights[i] = wr[i][0];
		
		double[][] ir = reader.Read(inputCSV, 43, 1);
		double[] inputs = new double[ir.length];
		for (int i = 0; i < inputs.length; ++i)
			inputs[i] = ir[i][0];
		
		double[][] or = reader.Read(outputCSV, 12, 1);
		double[] outputs = new double[or.length];
		for (int i = 0; i < outputs.length; ++i)
			outputs[i] = or[i][0];
		
		int[] layers = new int[3];
		layers[0] = 43;
		layers[1] = 10;
		layers[2] = 12;
		EKFANN ekf = new EKFANN(layers);
		ekf.setWeights(weights);
		double [] act_output = ekf.execute(inputs);
		
		for (int i = 0; i < act_output.length; ++i)
			assertTrue(Math.abs(act_output[i] - outputs[i]) < 10E-4);
	}
	
	
	@Test public void backwardTest() throws Exception
	{
		// For test we have to set partial weight change to a relative large value
		// because the reference data from matlab has limitied precision
		
		EKFANN.weightChange = 1.27009514017118e-6;

		String weigthCSV1 = "doc/test/BackwardPropagation/orgweight.csv",
		  refOutCSV1 = "doc/test/BackwardPropagation/refout1.csv",
		  inputCSV1 = "doc/test/BackwardPropagation/input1.csv",
		  refOutCSV2 = "doc/test/BackwardPropagation/refout2.csv",
		  inputCSV2 = "doc/test/BackwardPropagation/input2.csv",
		  refwghtCSV1 = "doc/test/BackwardPropagation/weight1.csv",
		  refPCSV1 = "doc/test/BackwardPropagation/P1.csv",
		  refwghtCSV2 = "doc/test/BackwardPropagation/weight2.csv",
		  refPCSV2 = "doc/test/BackwardPropagation/P2.csv";
		
		ReadFromCSV reader = new ReadFromCSV();
		
		double[][] wr = reader.Read(weigthCSV1, 572, 1);
		double[] weights = new double[wr.length];
		for (int i = 0; i < weights.length; ++i)
			weights[i] = wr[i][0];
		
		double[][] or = reader.Read(refOutCSV1, 12, 1);
		double[] outs1 = new double[or.length];
		for (int i = 0; i < outs1.length; ++i)
			outs1[i] = or[i][0];
		
		double[][] ir = reader.Read(inputCSV1, 43, 1);
		double[] inputs1 = new double[ir.length];
		for (int i = 0; i < inputs1.length; ++i)
			inputs1[i] = ir[i][0];
		
		or = reader.Read(refOutCSV2, 12, 1);
		double[] outs2 = new double[or.length];
		for (int i = 0; i < outs2.length; ++i)
			outs2[i] = or[i][0];
		
		ir = reader.Read(inputCSV2, 43, 1);
		double[] inputs2 = new double[ir.length];
		for (int i = 0; i < inputs2.length; ++i)
			inputs2[i] = ir[i][0];
		
		double[][] wrefr1 = reader.Read(refwghtCSV1, 572, 1);
		double[] wref1 = new double[wrefr1.length];
		for (int i = 0; i < wref1.length; ++i)
			wref1[i] = wrefr1[i][0];

		double[][] pref1 = reader.Read(refPCSV1, 572, 572);
		
		double[][] wrefr2 = reader.Read(refwghtCSV2, 572, 1);
		double[] wref2 = new double[wrefr2.length];
		for (int i = 0; i < wref2.length; ++i)
			wref2[i] = wrefr2[i][0];

		double[][] pref2 = reader.Read(refPCSV2, 572, 572);
		
		int[] layers = new int[3];
		layers[0] = 43;
		layers[1] = 10;
		layers[2] = 12;
		EKFANN ekf = new EKFANN(layers);
		ekf.setWeights(weights);
		
		Matrix P = Matrix.identityMatrix(weights.length); 
		double gap = 10E-6;

		// First iteration
		ekf.backwardPropagate(inputs1, outs1, weights, P);
		for (int i = 0; i < 572; ++i) {
			assertTrue(Math.abs(weights[i] - wref1[i]) < gap);
			for (int j = 0; j < 572; ++j)
				assertTrue(Math.abs(P.getVal(i, j) - pref1[i][j]) < gap);
		}
		ekf.setWeights(weights);
		
		weights = ekf.getWeights();
		// Second iteration
		ekf.backwardPropagate(inputs2, outs2, weights, P);
		for (int i = 0; i < 572; ++i) {
			assertTrue(Math.abs(weights[i] - wref2[i]) < gap);
			for (int j = 0; j < 572; ++j)
				assertTrue(Math.abs(P.getVal(i, j) - pref2[i][j]) < gap);
		}
	}
}
