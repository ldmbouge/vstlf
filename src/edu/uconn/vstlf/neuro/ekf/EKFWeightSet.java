package edu.uconn.vstlf.neuro.ekf;

import org.garret.perst.Persistent;

import edu.uconn.vstlf.neuro.WeightObj;

public class EKFWeightSet extends Persistent{
	
	long _id;
	public int[] _lyrSz;
	public WeightObj[] _weights;
	private double[] P_;

	WeightObj[] getWeights() { return _weights; }

	public EKFWeightSet(int id, int[] lyrSz, WeightObj[] weights, double[] P){
		_id = id;
		_lyrSz = lyrSz;
		_weights = weights;
		P_ = P;
	}

	public double[] getP() { return P_; }
	
	/*
	public boolean equals(Object o) {
		return super.equals(o);
	}
	public int hashCode() { return 7;}
	*/
}
