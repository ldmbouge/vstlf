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

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;
import org.garret.perst.*;
import java.util.logging.*;

public class EKFANN implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3173950989737199131L;
	//old vars
	double[][][] _weights;
	double[][][] _recentWeights;
	double[][] _output;
	double[][] _error;
	int _nbLayers;
	int[] _lyrSz;
	
	//new vars
	double _rNoise;
	double[][][] _errGrad;
	double[][][] _recentErrGrad;
	double[][][] _modelGrad;
	double[][][] _recentModelGrad;
	double[][][] _inGrad;
	double[][][] _fVec;
	double[][][] _vVec;
	double[][][][] _covUD;
	double[][] _epsilon;
	double[] _finalInnov;// = .002;
	

    private Logger _logger = Logger.getLogger("EKFANN");
	/**
	 * Constructs an ANN from two 3D arrays, one containing current weights, one recent weights.
	 * The matrices are indexed in the following way.
	 * Dim1: LayerID
	 * Dim2: Position of the node within the layer
	 * Dim3: Position of the node from which input is assigned this weight in the previous layer.
	 * @param w
	 * @param rw
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public EKFANN(double[][][][] w,double[][][][] e,double[][][][] m,double[][][] i) throws SecurityException, IOException{
		_weights = new double[w[0].length][][];			//allocate memory for weights
		_recentWeights = new double[w[1].length][][];	//	and last weights
		_errGrad = new double[e[0].length][][];			//		current error gradient
		_recentErrGrad = new double[e[1].length][][];	//		last error gradient
		_modelGrad = new double[m[0].length][][];		//		current model gradient
		_recentModelGrad = new double[m[1].length][][];	//		last model gradient
		_inGrad = new double[i.length][][];				//		input gradient
		_fVec = new double[w[0].length][][];			//		F
		_vVec = new double[w[0].length][][];			//		V
		_covUD = new double[w[0].length][][][];			//		Covariance UD
		_epsilon = new double[w[0].length][];			//		I don't know what epsilon is
		for(int lid=0;lid<w[0].length;lid++){
			_weights[lid] = new double[w[0][lid].length][];
			_recentWeights[lid] = new double[w[1][lid].length][];
			_errGrad[lid] = new double[e[0][lid].length][];
			_recentErrGrad[lid] = new double[e[1][lid].length][];
			_modelGrad[lid] = new double[m[0][lid].length][];
			_recentModelGrad[lid] = new double[m[1][lid].length][];
			_inGrad[lid] = new double[i[lid].length][];
			_fVec[lid] = new double[w[0][lid].length][];
			_vVec[lid] = new double[w[0][lid].length][];
			_covUD[lid] = new double[w[0][lid].length][][];
			_epsilon[lid] = new double[w[0][lid].length];
			for(int nid = 0;nid<w[0][lid].length;nid++){
				_weights[lid][nid] = new double[w[0][lid][nid].length];
				_recentWeights[lid][nid] = new double[w[1][lid][nid].length];
				_errGrad[lid][nid] = new double[e[0][lid][nid].length];
				_recentErrGrad[lid][nid] = new double[e[1][lid][nid].length];
				_modelGrad[lid][nid] = new double[m[0][lid][nid].length];
				_recentModelGrad[lid][nid] = new double[m[1][lid][nid].length];
				_inGrad[lid][nid] = new double[i[lid][nid].length];
				_fVec[lid][nid] = new double[i[lid][nid].length];
				_vVec[lid][nid] = new double[i[lid][nid].length];
				_covUD[lid][nid] = new double[i[lid][nid].length][i[lid][nid].length];
				for(int wid = 0;wid<w[0][lid][nid].length;wid++){		//store the given values in the new memory
					_weights[lid][nid][wid] = w[0][lid][nid][wid];
					_recentWeights[lid][nid][wid] = w[1][lid][nid][wid];
					_errGrad[lid][nid][wid] = e[0][lid][nid][wid];
					_recentErrGrad[lid][nid][wid] = e[1][lid][nid][wid];
					_modelGrad[lid][nid][wid] = m[0][lid][nid][wid];
					_recentModelGrad[lid][nid][wid] = m[1][lid][nid][wid];
					_inGrad[lid][nid][wid] = i[lid][nid][wid];
					for(int x=0;x<_covUD[lid][nid][wid].length;x++)
						for(int y=0;y<_covUD[lid][nid][wid].length;y++)
							_covUD[lid][nid][x][y] = (x==y)? .01:0;								
				}
			}
		}
		_output = new double[_weights.length][];							//to store the activation value of each node
		_error = new double[_weights.length][];								// to sore the backprop error at each node
		_nbLayers = _output.length;											// the number of layers of neurons
		for(int lid = 0;lid<_output.length;lid++){
			_output[lid] = new double[_weights[lid].length];
			_output[lid][0] = -1;
			_error[lid] = new double[_weights[lid].length];
		}
		_finalInnov = new double[_output[_output.length-1].length];
		_rNoise = .0001;														//some random noise
		_logger.addHandler(new FileHandler("ann.log"));
	}
	
	public EKFANN(double[][][][] w,double[][][][] e,double[][][][] m,double[][][] i, int[] lyrSz) throws SecurityException, IOException{
		this(w,e,m,i);
		_lyrSz = lyrSz;
	}
	
	public static EKFANN newUntrainedANN(int[] lyrSz) throws SecurityException, IOException{	//lyrSz specifies the network topology
		double[][][] w = new double[lyrSz.length][][];
		double[][][] r = new double[lyrSz.length][][];
		double[][][] errG = new double[lyrSz.length][][];
		double[][][] rErrG = new double[lyrSz.length][][];
		double[][][] modG = new double[lyrSz.length][][];
		double[][][] rModG = new double[lyrSz.length][][];
		double[][][] inG = new double[lyrSz.length][][];
		for(int lid = 0;lid<w.length;lid++){
			w[lid] = new double[lyrSz[lid]+1][];
			r[lid] = new double[lyrSz[lid]+1][];
			errG[lid] = new double[lyrSz[lid]+1][];
			rErrG[lid] = new double[lyrSz[lid]+1][];
			modG[lid] = new double[lyrSz[lid]+1][];
			rModG[lid] = new double[lyrSz[lid]+1][];
			inG[lid] = new double[lyrSz[lid]+1][];
			for(int nid = 0;nid<w[lid].length;nid++){
				w[lid][nid] = 		new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				r[lid][nid] = 		new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				errG[lid][nid] = 	new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				rErrG[lid][nid] = 	new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				modG[lid][nid] = 	new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				rModG[lid][nid] = 	new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				inG[lid][nid] = 	new double[(lid==0||nid==0)?0:lyrSz[lid-1]+1];
				for(int cid = 0;cid<w[lid][nid].length;cid++){	//weights and gradients are randomly generated
					w[lid][nid][cid] = 			4.8 * (Math.random() - 0.5) / ((double)w[lid][nid].length);//*/.5;
					r[lid][nid][cid] = 			4.8 * (Math.random() - 0.5) / ((double)w[lid][nid].length);//*/.5;
					errG[lid][nid][cid] = 		4.8 * (Math.random() - 0.5) / ((double)w[lid][nid].length);//*/.5;
					rErrG[lid][nid][cid] = 		4.8 * (Math.random() - 0.5) / ((double)w[lid][nid].length);//*/.5;
					modG[lid][nid][cid] = 		4.8 * (Math.random() - 0.5) / ((double)w[lid][nid].length);//*/.5;
					rModG[lid][nid][cid] = 		4.8 * (Math.random() - 0.5) / ((double)w[lid][nid].length);//*/.5;
					inG[lid][nid][cid] = rModG[lid][nid][cid];		
				}
			}
		}
		double[][][][] weightMat = {w,r};
		double[][][][] errMat = {errG,rErrG};
		double[][][][] modMat = {modG,rModG};
		EKFANN newANN = new EKFANN(weightMat,errMat,modMat,inG,lyrSz);	//construct a new net with these weights and gradients
		return newANN;
	}

	
	//Compute the innovation covariance
	public double computeInnovCov(double[] inVar, int outid){
		_finalInnov[outid+1] = gradient(outid+1) + _rNoise;					//get the gradient & noise
		for(int lid = _output.length-1;lid > 0;lid--)
			_finalInnov[outid+1] += computeInputGradient(lid, inVar);
		return 	_finalInnov[outid+1];					//return sum
	}
	
	
	public void EKFUpdate(double[] targetOut, int outid){
			for(int lid=_lyrSz.length-1;lid>0;lid--){
				for(int nid = 1;nid<_output[lid].length;nid++){
					if(lid < _lyrSz.length-1 || nid == outid+1){
						double dc;
						double beta;
						double[] lambda;
						int nbWeights = _output[lid-1].length;
						lambda=new double [nbWeights];
						dc=_finalInnov[outid];
						for (int wid = nbWeights-1;wid>0;wid--){ //for all weights except the bias
							beta=_covUD[lid][nid][wid][wid];
							_covUD[lid][nid][wid][wid]+=dc*_vVec[lid][nid][wid]*_vVec[lid][nid][wid]; //25
							dc*=beta/_covUD[lid][nid][wid][wid];//26
							lambda[wid]=_fVec[lid][nid][wid]*dc;//27
						}
						for(int wid=1;wid<nbWeights;wid++){
							for(int i=0;i<wid;i++){
								beta=_covUD[lid][nid][wid][wid];
								_covUD[lid][nid][i][wid]=beta+_vVec[lid][nid][i]*lambda[wid];//28
								_vVec[lid][nid][i]+=_vVec[lid][nid][wid]*beta;//29
							}
						}
						for (int wid=0;wid<nbWeights;wid++){
							_recentWeights[lid][nid][wid]=_weights[lid][nid][wid];
							_weights[lid][nid][wid]+=(_vVec[lid][nid][wid]*(targetOut[outid]-_output[_output.length-1][outid+1])/_finalInnov[outid+1]);
							//30&31
						}
					}
				}
			}
	}
	
	double gradient(int outid){
		boolean outLyr = true;
		double sum = 0;
		for(int lid = _lyrSz.length-1;lid>0;lid--){
			if(outLyr){
				for(int nid=1;nid<_output[lid].length;nid++){
					if(nid == outid){
						_epsilon[lid][nid] = _output[lid][nid]*(1-_output[lid][nid]);
						for(int wid=0;wid<_weights[lid][nid].length;wid++){
							_recentModelGrad[lid][nid][wid] = _modelGrad[lid][nid][wid];
							_modelGrad[lid][nid][wid] = _epsilon[lid][nid]*_output[lid-1][wid];
						}
						for(int wid=1;wid<_weights[lid][nid].length;wid++){  //do bias after loop
							_fVec[lid][nid][wid]=_modelGrad[lid][nid][wid];
							for (int k=0;k<(wid);k++){
								_fVec[lid][nid][wid] += _modelGrad[lid][nid][wid]*_covUD[lid][nid][k][wid];
							}
							_vVec[lid][nid][wid]=_covUD[lid][nid][wid][wid]*_fVec[lid][nid][wid];
							sum += _vVec[lid][nid][wid]*_fVec[lid][nid][wid];
						}
						_fVec[lid][nid][0]=_modelGrad[lid][nid][0];
						_vVec[lid][nid][0]=_covUD[lid][nid][0][0]*_fVec[lid][nid][0];
						sum += _vVec[lid][nid][0]*_fVec[lid][nid][0];
					}	
				}
				outLyr = false;
			}
			else{	//IDENTICAL, EXCEPT FOR THE VALUE OF EPSILON
				for(int nid=1;nid<_output[lid].length;nid++){
					_epsilon[lid][nid] = _output[lid][nid]*(1-_output[lid][nid])*layerEpsilon(lid+1,nid,outid);
					for(int wid=0;wid<_weights[lid][nid].length;wid++){
						_inGrad[lid][nid][wid]=_epsilon[lid][nid]*_weights[lid][nid][wid];
						_recentModelGrad[lid][nid][wid] = _modelGrad[lid][nid][wid];
						_modelGrad[lid][nid][wid] = _epsilon[lid][nid]*_output[lid-1][wid];
					}
					for(int wid=1;wid<_weights[lid][nid].length;wid++){  //do bias after loop
						_fVec[lid][nid][wid]=_modelGrad[lid][nid][wid];
						for (int k=0;k<(wid);k++){
							_fVec[lid][nid][wid] += _modelGrad[lid][nid][wid]*_covUD[lid][nid][k][wid];
						}
						_vVec[lid][nid][wid]=_covUD[lid][nid][wid][wid]*_fVec[lid][nid][wid];
						sum += _vVec[lid][nid][wid]*_fVec[lid][nid][wid];
					}
					_fVec[lid][nid][0]=_modelGrad[lid][nid][0];
					_vVec[lid][nid][0]=_covUD[lid][nid][0][0]*_fVec[lid][nid][0];
					sum += _vVec[lid][nid][0]*_fVec[lid][nid][0];
				}
			}
		}
		return sum;
	}
	
	
	
	double layerEpsilon(int lid, int wid, int outid){
		double sum = 0;
		for(int nid = 1; nid < _output[lid].length;nid++){
			if(nid==outid)
				sum += (_weights[lid][nid][wid]*_epsilon[lid][nid]);
		}
		return sum;
	}

	double computeInputGradient(int lid, double[] inVar){
		if(lid == _lyrSz.length-1)
			return 0;
		double[] inGrad=new double [_output[lid-1].length-1];
		int i=0;
		double sum=0.0;
		for(i=0;i<inGrad.length;i++){
			inGrad[i]=0.0;
			for(int nid = 1; nid<_output[lid].length;nid++){
				inGrad[i] += _inGrad[lid][nid][i];
			}
		}
		for(i=0;i<inGrad.length;i++){
			sum += inGrad[i]*inGrad[i]*inVar[i];
		}
		return sum;
	}


///////
////////////EXECUTE
////////
	public double[] execute(){
		//Feed the values forward through the net.
		for(int lid = 1;lid<_nbLayers;lid++){
			for(int nid = 1; nid<_output[lid].length;nid++){
				_output[lid][nid] = 0;
				for(int wid = 0; wid<_output[lid-1].length;wid++){
					_output[lid][nid] += (_output[lid-1][wid])*(_weights[lid][nid][wid]);
				}
				_output[lid][nid] = sigmoid(_output[lid][nid]);
			}
		}
		return getOutput();
	}
	
	/**
	 * Performs the typical feed forward neural net computation.
	 * @param input Array of values presented to the input layer.
	 */
	public double[] execute(double[] input){
		if(input.length!=_output[0].length-1) 
			_logger.fine(input.length+"!="+(_output[0].length-1));
		for(int i = 1;i<_output[0].length;i++){
			_output[0][i] = input[i-1];			//input i is layer 0, index i
		}
		return execute();
	}
	
	
	/**
	 * Executes the back-propagation learning routine.
	 * @param desiredOut The output that we'd like the net to have produced from the last input.
	 */
	public void update(double[] desiredOut){
		//Set the errors of the output nodes and update their weights.
		for(int nid = 1;nid<_output[_nbLayers-1].length;nid++){
			_error[_nbLayers-1][nid] = (desiredOut[nid-1] - _output[_nbLayers-1][nid]) * _output[_nbLayers-1][nid] * (1 - _output[_nbLayers-1][nid]);
			for(int sid = 0;sid<_output[_nbLayers-2].length;sid++){
				double deltaW = .2 * _error[_nbLayers-1][nid] * _output[_nbLayers-2][sid];
				double momentum = .35 * (_weights[_nbLayers-1][nid][sid] - _recentWeights[_nbLayers-1][nid][sid]);
				_recentWeights[_nbLayers-1][nid][sid] = _weights[_nbLayers-1][nid][sid];
				_weights[_nbLayers-1][nid][sid] += deltaW + momentum;
			}
		}
		//Propagate errors back through the net, and update the weights accordingly.
		for(int lid = _nbLayers-2;lid>0;lid--){
			for(int nid = 1;nid<_output[lid].length;nid++){
				_error[lid][nid] = 0;
				for(int rid = 1;rid<_output[lid+1].length;rid++){
					_error[lid][nid] += (_error[lid+1][rid])*(_weights[lid+1][rid][nid]);
				}
				_error[lid][nid] = (_output[lid][nid])*(1 - _output[lid][nid])*(_error[lid][nid]);
				for(int sid = 0;sid<_output[lid-1].length;sid++){
					double deltaW = .2 * _error[lid][nid] * _output[lid-1][sid];
					double momentum = .35 * (_weights[lid][nid][sid] -
											   _recentWeights[lid][nid][sid]);
					_recentWeights[lid][nid][sid] = _weights[lid][nid][sid];
					_weights[lid][nid][sid] += deltaW + momentum;
				}
			}
		}
	}
		
	/**
	 * Simple sigmoid squashing function.
	 * @param x A double.
	 * @return F(x).
	 */
	double sigmoid(double x){
		return 1/(1 + Math.exp(-x));
	}
	
	/**
	 * Retrieve the output produced by the net.
	 * @return A copy of the result of the output layer.
	 */
	public double[] getOutput(){
		double[] r = new double[_output[_nbLayers-1].length-1];
		System.arraycopy(_output[_nbLayers-1], 1, r, 0, _output[_nbLayers-1].length-1);
		return r;
	}
	
	
	public void EKFTrain(double[][] in, double[][] tg, double[][] inVar, int maxSeconds) throws Exception{
		_logger.fine("Training");
		long st = System.currentTimeMillis();
		long dt = maxSeconds*1000;
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		int e = 0;
		do{
			if(e++%100 == 0)
			    _logger.fine(Integer.toString(e));
			for(int i = 0;i<in.length;i++){
				execute(in[i]);
				for(int outid = 0; outid<_output[_output.length-1].length-1;outid++){
					computeInnovCov(inVar[i], outid);
					EKFUpdate(tg[i], outid);
				}
			}
		}while(System.currentTimeMillis() < st+dt);
		_logger.fine("\tDone.");
	}
	
	public void train(double[][] in, double[][] tg, int maxSeconds) throws Exception{
		_logger.fine("Training");
		long st = System.currentTimeMillis();
		long dt = maxSeconds*1000;
		//double[] zip = new double[_output[0].length-1];
		if(in.length!=tg.length) throw new Exception("You must have the same number of in and tg");
		int e = 0;
		do{
			if(e++%100 == 0)
			    _logger.fine(Integer.toString(e));
			for(int i = 0;i<in.length;i++){
				execute(in[i]);
				update(tg[i]);
			}
		}while(System.currentTimeMillis() < st+dt);
		_logger.fine("\tDone.");
	}
	
	public void setRNoise(double r){
		_rNoise = r;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////
	
	
	@SuppressWarnings("unchecked")
	public void save(String file){
		//Open storage
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<EKFThing> sets = (Index<EKFThing>)db.getRoot();
		if (sets == null) { 
            sets = db.createIndex(int.class, true);
            db.setRoot(sets);
        }
		
		//Weights
		Vector<EKFWeightObj> currV = new Vector<EKFWeightObj>(), pastV = new Vector<EKFWeightObj>();
		for(int lid = 0;lid<_nbLayers;lid++){
			for(int nid = 0;nid<_weights[lid].length;nid++){
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					currV.add(new EKFWeightObj(lid,nid,sid,_weights[lid][nid][sid]));
					pastV.add(new EKFWeightObj(lid,nid,sid,_recentWeights[lid][nid][sid]));
				}
			}
		}
		EKFWeightObj[] curr = new EKFWeightObj[currV.size()], past = new EKFWeightObj[pastV.size()];
		for(int i = 0; i<curr.length;i++){
			curr[i] = currV.elementAt(i);
			past[i] = pastV.elementAt(i);
		}
		EKFWeightSet content = new EKFWeightSet(_lyrSz,curr,past);
		if(sets.get(new Key(0))!=null) sets.remove(new Key(0));
		sets.put(new Key(0),content);
		
		//ModelGradient
		currV = new Vector<EKFWeightObj>(); pastV = new Vector<EKFWeightObj>();
		for(int lid = 0;lid<_nbLayers;lid++){
			for(int nid = 0;nid<_weights[lid].length;nid++){
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					currV.add(new EKFWeightObj(lid,nid,sid,_modelGrad[lid][nid][sid]));
					pastV.add(new EKFWeightObj(lid,nid,sid,_recentModelGrad[lid][nid][sid]));
				}
			}
		}
		curr = new EKFWeightObj[currV.size()]; past = new EKFWeightObj[pastV.size()];
		for(int i = 0; i<curr.length;i++){
			curr[i] = currV.elementAt(i);
			past[i] = pastV.elementAt(i);
		}
		content = new EKFWeightSet(_lyrSz,curr,past);
		if(sets.get(new Key(1))!=null) sets.remove(new Key(1));
		sets.put(new Key(1),content);
		
		//ErrorGradient
		currV = new Vector<EKFWeightObj>(); pastV = new Vector<EKFWeightObj>();
		for(int lid = 0;lid<_nbLayers;lid++){
			for(int nid = 0;nid<_weights[lid].length;nid++){
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					currV.add(new EKFWeightObj(lid,nid,sid,_errGrad[lid][nid][sid]));
					pastV.add(new EKFWeightObj(lid,nid,sid,_recentErrGrad[lid][nid][sid]));
				}
			}
		}
		curr = new EKFWeightObj[currV.size()]; past = new EKFWeightObj[pastV.size()];
		for(int i = 0; i<curr.length;i++){
			curr[i] = currV.elementAt(i);
			past[i] = pastV.elementAt(i);
		}
		content = new EKFWeightSet(_lyrSz,curr,past);
		if(sets.get(new Key(2))!=null) sets.remove(new Key(2));
		sets.put(new Key(2),content);
		
		//Covariance
		Vector<EKFCovarianceObj> currV2 = new Vector<EKFCovarianceObj>();
		for(int lid = 0;lid<_nbLayers;lid++){
			for(int nid = 0;nid<_weights[lid].length;nid++){
				for(int sid = 0;sid<_weights[lid][nid].length;sid++){
					for(int sid2 = 0; sid2<_weights[lid][nid].length;sid2++){
						currV2.add(new EKFCovarianceObj(lid,nid,sid,sid2,_covUD[lid][nid][sid][sid2]));
					}
				}
			}
		}
		EKFCovarianceObj[] curr2 = new EKFCovarianceObj[currV2.size()];
		//System.err.println();
		for(int i = 0; i<curr2.length;i++){
			curr2[i] = currV2.elementAt(i);
		}
		EKFCovarianceSet content2 = new EKFCovarianceSet(_lyrSz,curr2);
		if(sets.get(new Key(3))!=null) sets.remove(new Key(3));
		sets.put(new Key(3),content2);
		
		db.commit();
		db.close();
	}
	
	@SuppressWarnings("unchecked")
	public static EKFANN load(String file) throws Exception {
		//open storage
		Storage db = StorageFactory.getInstance().createStorage();
		db.open(file, Storage.DEFAULT_PAGE_POOL_SIZE);
		Index<EKFThing> sets = (Index<EKFThing>)db.getRoot();
		if (sets == null) { 
            throw new Exception("There are no ANNs stored in the file '" + file +"'.");
        }
		//get weights, gradients, and covUD
		EKFWeightSet weights = (EKFWeightSet)sets.get(new Key(0));
		if(weights==null){
			throw new Exception("weights are not stored in the file '" + file +"'.");
		}
		EKFWeightSet mGrad = (EKFWeightSet)sets.get(new Key(1));
		if(mGrad==null){
			throw new Exception("model gradient is not stored in the file '" + file +"'.");
		}
		EKFWeightSet eGrad = (EKFWeightSet)sets.get(new Key(2));
		if(eGrad==null){
			throw new Exception("error gradient is not stored in the file '" + file +"'.");
		}
		EKFCovarianceSet ud = (EKFCovarianceSet)sets.get(new Key(3));
		if(ud==null){
			throw new Exception("Covariance matrix not stored in the file '" + file +"'.");
		}
		
		//make the ann
		EKFANN ann = EKFANN.newUntrainedANN(weights._lyrSz);
		
		//fill the ann with db values
		EKFWeightObj[] curr = weights._curr, past = weights._past;
		for(int i = 0;i<weights._curr.length;i++){
			ann._weights[curr[i].lid()][curr[i].nid()][curr[i].cid()] = curr[i].val();
			ann._recentWeights[past[i].lid()][past[i].nid()][past[i].cid()] = past[i].val();
		}
		curr = mGrad._curr; past = mGrad._past;
		for(int i = 0;i<weights._curr.length;i++){
			ann._modelGrad[curr[i].lid()][curr[i].nid()][curr[i].cid()] = curr[i].val();
			ann._recentModelGrad[past[i].lid()][past[i].nid()][past[i].cid()] = past[i].val();
		}
		curr = eGrad._curr; past = eGrad._past;
		for(int i = 0;i<weights._curr.length;i++){
			ann._errGrad[curr[i].lid()][curr[i].nid()][curr[i].cid()] = curr[i].val();
			ann._recentErrGrad[past[i].lid()][past[i].nid()][past[i].cid()] = past[i].val();
		}
		EKFCovarianceObj[] cov = ud._objs;
		for(int i = 0;i<cov.length;i++)
			ann._covUD[cov[i].i()][cov[i].j()][cov[i].k()][cov[i].l()] = cov[i].val();
		return ann;
	}

	////////////////////////////////
	/////////////////////////////////
	/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
	}//*/
}

interface EKFThing extends IPersistent{}

class EKFWeightObj extends Persistent implements EKFThing,Serializable{
	int _lid,_nid,_cid;
	double _val;
	EKFWeightObj(int lid, int nid, int cid, double val){
		_lid = lid; _nid = nid; _cid = cid; _val = val;
	}
	int lid(){return _lid;}
	int nid(){return _nid;}
	int cid(){return _cid;}
	double val(){return _val;}
	
	/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
	}//*/
}

class EKFWeightSet extends Persistent implements EKFThing,Serializable{
	int[] _lyrSz;
	EKFWeightObj[] _curr;
	EKFWeightObj[] _past;
	EKFWeightSet(int[] lyrSz, EKFWeightObj[] curr, EKFWeightObj[] past){
		_curr = curr; _past = past; _lyrSz = lyrSz;
	}
	/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
	}//*/
}

class EKFCovarianceObj extends Persistent implements EKFThing,Serializable{
	int _i, _j, _k, _l;
	double _val;
	EKFCovarianceObj(int i, int j, int k, int l, double val){
		_i = i; _j = j; _k = k; _l = l; _val = val;
	}
	int i(){return _i;}
	int j(){return _j;}
	int k(){return _k;}
	int l(){return _l;}
	double val(){return _val;}
	/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
	}//*/
}

class EKFCovarianceSet extends Persistent implements EKFThing,Serializable{
	int[] _lyrSz;
	EKFCovarianceObj[] _objs;
	EKFCovarianceSet(int[] lyrSz, EKFCovarianceObj[] objs){
		_lyrSz = lyrSz; _objs = objs;
	}
	/*
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
	}//*/
}



