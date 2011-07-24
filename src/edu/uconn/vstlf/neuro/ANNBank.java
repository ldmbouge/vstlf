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

import java.util.logging.Level;

import org.garret.perst.Index;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;

import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
public class ANNBank{
	
	SimpleFeedForwardANN[] _anns;
	private boolean[] _disableANNs;
	private double[] _accErrs;
	private int[] _accPoints;
	
	public ANNBank(int[][] lyrSz){
		_anns = new SimpleFeedForwardANN[lyrSz.length];
		for(int i = 0;i<lyrSz.length;i++)
			_anns[i] = SimpleFeedForwardANN.newUntrainedANN(lyrSz[i]);
		_disableANNs = new boolean[_anns.length];
		_accErrs = new double[_anns.length];
		_accPoints = new int[_anns.length];
	}
	
	@SuppressWarnings("unchecked")
	public ANNBank(String file)throws Exception{
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
		_disableANNs = new boolean[_anns.length];
		_accErrs = new double[_anns.length];
		_accPoints = new int[_anns.length];
	}
	
	public void save(String file)throws Exception{
		for(int id=0;id<_anns.length;id++) _anns[id].save(file, id);
	}
	
	/*
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
	*/
	
	public Series[] execute(Series[] in)throws Exception{
		Series[] out = new Series[_anns.length];
		for(int i = 0;i<out.length;i++)
			if (!_disableANNs[i])
				out[i] = _anns[i].execute(in[i]);
			else
				out[i] = new Series(_anns[i].getOutput().length);
		return out;
	}
	
	public void update(Series[] tg)throws Exception{
		for(int i = 0;i<_anns.length;i++)
			if (!_disableANNs[i]) {
				_anns[i].update(tg[i]);
				logAccErrors(i, tg[i], new Series(_anns[i].getOutput(), false));
			}
	}
	
	public void addANN(int i,double[][][][] w, int[] lyrSz){
		_anns[i] = new SimpleFeedForwardANN(w,lyrSz);
	}
	
	public void disableNetwork(int i)
	{
		_disableANNs[i] = true;
	}
	
	public void enableNetwork(int i)
	{
		_disableANNs[i] = false;
	}
	
	public void resetAccError(int netId)
	{
		_accErrs[netId] = 0.0;
		_accPoints[netId] = 0;
	}
	
	private void logAccErrors(int netId, Series targ, Series out)
	{
		double[] t = targ.array(), o = out.array();
		for (int i = 0; i < t.length; ++i)
			_accErrs[netId] += Math.abs(t[i]-o[i]);
		_accPoints[netId] += t.length;
		if (_accPoints[netId] % (2000*t.length) == 0)
			MessageCenter.getInstance().put(new LogMessage(Level.INFO, "ANNBank", "logAccErrors", "AccError of network " + netId + ": " + (_accErrs[netId]/_accPoints[netId])));
	}
}
