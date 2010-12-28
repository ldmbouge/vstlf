package edu.uconn.vstlf.neuro;

import org.garret.perst.Persistent;

public class WeightObj extends Persistent{
	int _lid,_nid,_cid;
	double _val;
	public WeightObj() {}
	public WeightObj(int lid, int nid, int cid, double val){
		_lid = lid; _nid = nid; _cid = cid; _val = val;
	}
	public int lid(){return _lid;}
	public int nid(){return _nid;}
	public int cid(){return _cid;}
	public double val(){return _val;}
	public boolean equals(Object o) {
		return super.equals(o);
	}
	public int hashCode() { return 7;}
}
/*
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
*/