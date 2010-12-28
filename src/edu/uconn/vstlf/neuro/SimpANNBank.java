/************************************************************************
 MIT License

 Copyright (c) 2010 University of Connecticut

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***********************************************************************/

package edu.uconn.vstlf.neuro;

import org.garret.perst.Index;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;

import edu.uconn.vstlf.data.doubleprecision.*;
public class SimpANNBank extends ANNBank{
	
	SimpleFeedForwardANN[] _anns;
	
	public SimpANNBank(int[][] lyrSz){
		_anns = new SimpleFeedForwardANN[lyrSz.length];
		for(int i = 0;i<lyrSz.length;i++)
			_anns[i] = SimpleFeedForwardANN.newUntrainedANN(lyrSz[i]);
		
	}
	
	@SuppressWarnings("unchecked")
	public SimpANNBank(String file)throws Exception{
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<WeightSet> sets = (Index<WeightSet>)db.getRoot();
		if (sets == null) { 
            throw new Exception("There are no ANNs stored in the file '" + file +"'.");
        }
		int nbnn = sets.size();
		db.close();
		_anns = new SimpleFeedForwardANN[nbnn];
		for(int id = 0;id<nbnn;id++){
			_anns[id] = SimpleFeedForwardANN.load(file, id);
		}
	}
	
	public void save(String file)throws Exception{
		for(int id=0;id<_anns.length;id++) _anns[id].save(file, id);
	}
	
	public void train(Series[][] in, Series[][] tg, double[] err)throws Exception{
		Series[][] inf = new Series[in[0].length][in.length];
		Series[][] tgf = new Series[tg[0].length][tg.length];
		for(int i = 0;i<in.length;i++){
			for(int j = 0;j<inf.length;j++){
				inf[j][i] = in[i][j];
				tgf[j][i] = tg[i][j];
			}
			
		}
		for(int i = 0;i<_anns.length;i++){
			//System.out.println(inf[i][0].length());
			//System.out.println(inf[i][0]);
			//System.out.println(tgf[i][0].length());
			//System.out.println(tgf[i][0]);
			_anns[i].train(inf[i], tgf[i], err[i]);}
	}
	
	public void train(Series[][] in, Series[][] tg, int[] seconds)throws Exception{
		Series[][] inf = new Series[in[0].length][in.length];
		Series[][] tgf = new Series[tg[0].length][tg.length];
		for(int i = 0;i<in.length;i++){
			for(int j = 0;j<inf.length;j++){
				inf[j][i] = in[i][j];
				tgf[j][i] = tg[i][j];
			}
			
		}
		for(int i = 0;i<_anns.length;i++)
			_anns[i].train(inf[i], tgf[i], seconds[i]);
	}
	
	public void train(Series[][] in, Series[][] tg, int[] seconds, double[] err)throws Exception{
		Series[][] inf = new Series[in[0].length][in.length];
		Series[][] tgf = new Series[tg[0].length][tg.length];
		for(int i = 0;i<in.length;i++){
			for(int j = 0;j<inf.length;j++){
				inf[j][i] = in[i][j];
				tgf[j][i] = tg[i][j];
			}
			
		}
		for(int i = 0;i<_anns.length;i++)
			_anns[i].train(inf[i], tgf[i], seconds[i],err[i]);
	}
	
	public Series[] execute(Series[] in)throws Exception{
		Series[] out = new Series[_anns.length];
		for(int i = 0;i<out.length;i++)
			out[i] = _anns[i].execute(in[i]);
		return out;
	}
	
	public void update(Series[] tg)throws Exception{
		for(int i = 0;i<_anns.length;i++)
			_anns[i].update(tg[i]);
	}
	
	public void addANN(int i,double[][][][] w, int[] lyrSz){
		_anns[i] = new SimpleFeedForwardANN(w,lyrSz);
	}
	
}

/*
class WeightSet extends Persistent{
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


class WeightObj extends Persistent{
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
*/