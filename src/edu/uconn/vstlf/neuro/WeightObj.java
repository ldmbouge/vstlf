package edu.uconn.vstlf.neuro;

import org.garret.perst.Persistent;

class WeightObj extends Persistent {
	int _lid,_nid,_cid;
	double _val;
	public WeightObj() {}
	WeightObj(int lid, int nid, int cid, double val){
		_lid = lid; _nid = nid; _cid = cid; _val = val;
	}
	int lid(){return _lid;}
	int nid(){return _nid;}
	int cid(){return _cid;}
	double val(){return _val;}
	public boolean equals(Object o) {
		return super.equals(o);
	}
	public int hashCode() { return 7;}
}
