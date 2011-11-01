package edu.uconn.vstlf.neuro;

import org.garret.perst.Persistent;

class WeightSet extends Persistent {
	long _id;
	int[] _lyrSz;
	WeightObj[] _curr;
	WeightObj[] _past;
	public WeightSet() {
		_id = -1;
		_lyrSz = null;
		_curr = _past = null;
	}
	WeightSet(int id, int[] lyrSz, WeightObj[] curr, WeightObj[] past){
		_id = id; _curr = curr; _past = past; _lyrSz = lyrSz;
	}
	public boolean equals(Object o) {
		return super.equals(o);
	}
	public int hashCode() { return 7;}
}
