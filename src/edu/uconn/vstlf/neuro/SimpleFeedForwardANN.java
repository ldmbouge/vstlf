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

import edu.uconn.vstlf.data.doubleprecision.MeanFunction;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;

import org.garret.perst.*;
import java.util.Vector;
import java.util.logging.Level;
public class SimpleFeedForwardANN {
	
	private static final double ALPHA = .35;
	private static final double ETA = .2;
	private static final double LAMBDA = 1.08;
	
	double[][][] _weights;
	double[][][] _recentWeights;
	double[][] _output;
	double[][] _error;
	int _nbLayers;
	int[] _lyrSz;
	
	/**
	 * Constructs an ANN from two 3D arrays, one containing current weights, one recent weights.
	 * The matrices are indexed in the following way.
	 * Dim1: LayerID
	 * Dim2: Position of the node within the layer
	 * Dim3: Position of the node from which input is assigned this weight in the previous layer.
	 * @param w
	 * @param rw
	 */
	public SimpleFeedForwardANN(double[][][][] w){
		_weights = new double[w[0].length][][];
		_recentWeights = new double[w[1].length][][];
		for(int lid=0;lid<w[0].length;lid++){
			_weights[lid] = new double[w[0][lid].length][];
			_recentWeights[lid] = new double[w[1][lid].length][];
			for(int nid = 0;nid<w[0][lid].length;nid++){
				_weights[lid][nid] = new double[w[0][lid][nid].length];
				_recentWeights[lid][nid] = new double[w[1][lid][nid].length];
				for(int snid = 0;snid<w[0][lid][nid].length;snid++){
					_weights[lid][nid][snid] = w[0][lid][nid][snid];
					_recentWeights[lid][nid][snid] = w[1][lid][nid][snid];
				}
			}
		}
		_output = new double[_weights.length][];
		_error = new double[_weights.length][];
		_nbLayers = _output.length;
		for(int lid = 0;lid<_output.length;lid++){
			_output[lid] = new double[_weights[lid].length];
			_error[lid] = new double[_weights[lid].length];
		}
	}
	
	public SimpleFeedForwardANN(double[][][][] w, int[] lyrSz){
		this(w);
		_lyrSz = lyrSz;
	}
	
	public static SimpleFeedForwardANN newUntrainedANN(int[] lyrSz){
		double[][][] w = new double[lyrSz.length][][];
		double[][][] r = new double[lyrSz.length][][];
		//double i = .5;int t = 868;
		//double z = .1;
		for(int lid = 0;lid<w.length;lid++){
			w[lid] = new double[lyrSz[lid]+1][];
			r[lid] = new double[lyrSz[lid]+1][];
			
			for(int nid = 0;nid<w[lid].length;nid++){
				w[lid][nid] = new double[(lid==0||nid==w[lid].length-1)?0:lyrSz[lid-1]+1];
				r[lid][nid] = new double[(lid==0||nid==r[lid].length-1)?0:lyrSz[lid-1]+1];
				
				for(int cid = 0;cid<w[lid][nid].length;cid++){
					w[lid][nid][cid] = 4.7 * (Math.random() - 0.5) / ((double)w[lid][nid].length);
					r[lid][nid][cid] = 4.7 * (Math.random() - 0.5) / ((double)w[lid][nid].length);
					//t-=3;z*=(t%4==0)?-1:1;z*=(t%2==0)?10:.09;i*=z; i+= .5*((double)cid/w[lid][nid].length);
					//if(i>1 || i<-1) i = 1.0/i;
					//w[lid][nid][cid] = i;
					//r[lid][nid][cid] = i/3.0;
				}
			}
		}
		double[][][][] weightMat = {w,r};
		return new SimpleFeedForwardANN(weightMat,lyrSz);
	}
	
	
	public double[] execute(){
		_output[0][_output[0].length-1] = -1; //fixed input of 1.
		//Feed the values forward through the net.
		for(int lid = 1;lid<_nbLayers;lid++){
			for(int nid = 0; nid<_output[lid].length-1;nid++){
				_output[lid][nid] = 0;
				for(int snid = 0; snid<_output[lid-1].length;snid++){
					_output[lid][nid] += (_output[lid-1][snid])*(_weights[lid][nid][snid]);
				}
				_output[lid][nid] = sigmoid(_output[lid][nid]);
			}
			_output[lid][_output[lid].length-1] = -1;//bias node.
		}
		return getOutput();
	}
	
	/**
	 * Performs the typical feed forward neural net computation.
	 * @param input Array of values presented to the input layer.
	 * @throws Exception 
	 */
	public double[] execute(double[] input) throws Exception{
		if(input.length!=_output[0].length-1) {
			MessageCenter.getInstance().put(new LogMessage(Level.INFO,
					SimpleFeedForwardANN.class.getName(), "execute(double[])", input.length+"!="+(_output[0].length-1)));
			throw new Exception("Input to the neural network is not compatible with the network layer out!");
		}
		//Set input values
		for(int i = 0;i<_output[0].length-1;i++){
			_output[0][i] = input[i];
		}
		return execute();
	}
	
	public double[] getInput()
	{
		double[] input = new double[_output[0].length-1];
		for(int i = 0;i<input.length ;i++){
			input[i] = _output[0][i];
		}
		return input;
	}
	
	public Series execute(Series in)throws Exception{
		return new Series(execute(in.array(false)),false);
	}
	
	/**
	 * Simple sigmoid squashing function.
	 * @param x A double.
	 * @return F(x).
	 */
	public double sigmoid(double x){
		return 1/(1 + Math.exp(-LAMBDA*x));
	}
	
	/**
	 * Retrieve the output produced by the net.
	 * @return A copy of the result of the output layer.
	 */
	public double[] getOutput(){
		double[] r = new double[_output[_nbLayers-1].length-1];
		System.arraycopy(_output[_nbLayers-1], 0, r, 0, _output[_nbLayers-1].length-1);
		return r;
	}
	
	/**
	 * Set the weight that the given node in the given layer is to place on input from the
	 * given node in the previous layer.
	 * @param layerID
	 * @param nodeID
	 * @param srcNodeID
	 * @param val
	 */
	public void setWeight(int layerID,int nodeID, int srcNodeID, double val){
		_recentWeights[layerID][nodeID][srcNodeID] = _weights[layerID][nodeID][srcNodeID];
		_weights[layerID][nodeID][srcNodeID] = val;
	}
	
	/**
	 * Executes the back-propagation learning routine.
	 * @param desiredOut The output that we'd like the net to have produced from the last input.
	 */
	public void update(double[] desiredOut){		
		//Set the errors of the output nodes and update their weights.
		for(int nid = 0;nid<_output[_nbLayers-1].length-1;nid++){
			_error[_nbLayers-1][nid] = (desiredOut[nid] - _output[_nbLayers-1][nid])*
									   _output[_nbLayers-1][nid]*
									   (1 - _output[_nbLayers-1][nid]);
			for(int sid = 0;sid<_output[_nbLayers-2].length;sid++){
				double deltaW = ETA * _error[_nbLayers-1][nid] * _output[_nbLayers-2][sid];
				double momentum = ALPHA * (_weights[_nbLayers-1][nid][sid] -
										   _recentWeights[_nbLayers-1][nid][sid]);
				_recentWeights[_nbLayers-1][nid][sid] = _weights[_nbLayers-1][nid][sid];
				_weights[_nbLayers-1][nid][sid] += deltaW + momentum;
			}
		}
		//Propagate errors back through the net, and update the weights accordingly.
		for(int lid = _nbLayers-2;lid>0;lid--){
			for(int nid = 0;nid<_output[lid].length-1;nid++){
				_error[lid][nid] = 0;
				for(int rid = 0;rid<_output[lid+1].length-1;rid++){
					_error[lid][nid] += (_error[lid+1][rid])*(_weights[lid+1][rid][nid]);
				}
				_error[lid][nid] = (_output[lid][nid])*(1 - _output[lid][nid])*(_error[lid][nid]);
				for(int sid = 0;sid<_output[lid-1].length;sid++){
					double deltaW = ETA * _error[lid][nid] * _output[lid-1][sid];
					double momentum = ALPHA * (_weights[lid][nid][sid] -
											   _recentWeights[lid][nid][sid]);
					_recentWeights[lid][nid][sid] = _weights[lid][nid][sid];
					_weights[lid][nid][sid] += deltaW + momentum;
				}
			}
		}
	}
	
	public void update(Series trg){
		update(trg.array(false));
	}
	
	/**
	 * Returns a 3D array containing this net's current weights.
	 */
	public double[][][] getWeights(){
		double[][][] r = new double[_nbLayers][][];
		for(int lid = 0;lid<r.length;lid++){
			r[lid] = new double[_weights[lid].length][];
			for(int nid = 0;nid<r[lid].length;nid++){
				r[lid][nid] = new double[_weights[lid][nid].length];
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					r[lid][nid][sid] = _weights[lid][nid][sid];
				}
			}
		}
		return r;
	}
	
	/**
	 * Returns a similar 3D array containing this net's recent weights.
	 */
	public double[][][] getRecentWeights(){
		double[][][] r = new double[_nbLayers][][];
		for(int lid = 0;lid<r.length;lid++){
			r[lid] = new double[_output[lid].length][];
			for(int nid = 0;nid<r[lid].length;nid++){
				r[lid][nid] = new double[_weights[lid][nid].length];
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					r[lid][nid][sid] = _recentWeights[lid][nid][sid];
				}
			}
		}
		return r;
	}
	
	public void train(Series[] in, Series[] tg,double err)throws Exception{
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "Training..."));
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		double[] mse = new double[in.length];
		do{
			for(int i = 0;i<in.length;i++){
				Series diff = tg[i].minus(execute(in[i]));
				mse[i] = diff.meanOfSquares();
				update(tg[i]);
			}
			//System.err.println((n++) +": "+new MeanFunction().imageOf(mse));
		}while(new MeanFunction().imageOf(mse) > err);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "\tDone."));
	}
	
	public void train(Series[] in, Series[] tg, int seconds)throws Exception{
		if(seconds==0)return;
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "Training..."));
		long st = System.currentTimeMillis();
		long dt = seconds*1000;
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		double[] mse = new double[in.length];
		do{
			for(int i = 0;i<in.length;i++){
				Series diff = tg[i].minus(execute(in[i]));
				mse[i] = diff.meanOfSquares();
				update(tg[i]);
			}
			//System.err.println((n++) +": "+new MeanFunction().imageOf(mse));
		}while(System.currentTimeMillis() < st+dt);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "\tDone."));
	}
	
	public void train(Series[] in, Series[] tg, int seconds,double err)throws Exception{
		if(seconds==0) return;
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "Training..."));
		long st = System.currentTimeMillis();
		long dt = seconds*1000;
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		double[] mse = new double[in.length];
		do{
			for(int i = 0;i<in.length;i++){
				Series diff = tg[i].minus(execute(in[i]));
				mse[i] = diff.meanOfSquares();
				update(tg[i]);
			}
			//System.err.println((n++) +": "+new MeanFunction().imageOf(mse));
		}while(System.currentTimeMillis() < st+dt && new MeanFunction().imageOf(mse) > err);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "\tDone."));
	}
	
	public void train(double[][] in, double[][] tg,double err)throws Exception{
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "Training..."));
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		int n =0;
		double[] mse = new double[in.length];
		do{
			for(int i = 0;i<in.length;i++){
				Series tgt = new Series(tg[i],false);
				Series inp = new Series(in[i],false);
				Series diff = tgt.minus(execute(inp));
				mse[i] = diff.meanOfSquares();
				update(tg[i]);
			}
			MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, (n++) +": "+new MeanFunction().imageOf(mse)));
		}while(new MeanFunction().imageOf(mse) > err);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "\tDone."));
	}
	
	public void train(double[][] in, double[][] tg,int seconds)throws Exception{
		String methodName = "train";
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "Training..."));
		long st = System.currentTimeMillis();
		long dt = seconds*1000;
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		double[] mse = new double[in.length];
		do{
			for(int i = 0;i<in.length;i++){
				Series tgt = new Series(tg[i],false);
				Series inp = new Series(in[i],false);
				Series diff = tgt.minus(execute(inp));
				mse[i] = diff.meanOfSquares();
				update(tg[i]);
			}
			//System.err.println((n++) +": "+new MeanFunction().imageOf(mse));
		}while(System.currentTimeMillis() < st+dt);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, SimpleFeedForwardANN.class.getName(), methodName, "\tDone."));
	}
	
	@SuppressWarnings("unchecked")
	public void save(String file, int id){
		Vector<WeightObj> currV = new Vector<WeightObj>(), pastV = new Vector<WeightObj>();
		for(int lid = 0;lid<_nbLayers;lid++){
			for(int nid = 0;nid<_weights[lid].length;nid++){
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					currV.add(new WeightObj(lid,nid,sid,_weights[lid][nid][sid]));
					pastV.add(new WeightObj(lid,nid,sid,_recentWeights[lid][nid][sid]));
				}
			}
		}
		WeightObj[] curr = new WeightObj[currV.size()], past = new WeightObj[pastV.size()];
		for(int i = 0; i<curr.length;i++){
			curr[i] = currV.elementAt(i);
			past[i] = pastV.elementAt(i);
		}
		WeightSet content = new WeightSet(id,_lyrSz,curr,past);
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<WeightSet> sets = (Index<WeightSet>)db.getRoot();
		if (sets == null) { 
            sets = db.createIndex(int.class, true);
            db.setRoot(sets);
        }
		if(sets.get(new Key(id))!=null) sets.remove(new Key(id));
		sets.put(new Key(id),content);
		db.commit();
		db.close();
	}
	
	@SuppressWarnings("unchecked")
	public static SimpleFeedForwardANN load(String file, int id)throws Exception{
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<WeightSet> sets = (Index<WeightSet>)db.getRoot();
		if (sets == null) { 
            throw new Exception("There are no ANNs stored in the file '" + file +"'.");
        }
		WeightSet content = sets.get(new Key(id));
		if(content==null){
			throw new Exception("ANN #"+id+"is not stored in the file '" + file +"'.");
		}
		SimpleFeedForwardANN ann = SimpleFeedForwardANN.newUntrainedANN(content._lyrSz);
		WeightObj[] curr = content._curr, past = content._past;
		for(int i = 0;i<content._curr.length;i++){
			ann._weights[curr[i].lid()][curr[i].nid()][curr[i].cid()] = curr[i].val();
			ann._recentWeights[past[i].lid()][past[i].nid()][past[i].cid()] = past[i].val();
		}
		return ann;
	}
	
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



