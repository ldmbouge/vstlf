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

public class XOREKFTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int[] lyrSz = {3,5,3};
		EKFANN ann = EKFANN.newUntrainedANN(lyrSz);
		System.out.println("Starting Up");
		double[][] in = {
				{.1,.1,.1},
				{.1,.1,.9},
				{.1,.9,.1},
				{.1,.9,.9},
				{.9,.1,.1},
				{.9,.1,.9},
				{.9,.9,.1},
				{.9,.9,.9}
		};
		double[][] tg = {
				{.1,.1,.1},
				{.9,.1,.1},
				{.1,.9,.1},
				{.9,.9,.1},
				{.1,.1,.9},
				{.9,.1,.9},
				{.1,.9,.9},
				{.9,.9,.9}	
		};
		double[][] inVar  = new double[8][];
		for(int i = 0;i<8;i++)
			inVar[i] = invar(in[i]);
		try{
			ann.EKFTrain(in, tg, inVar, 5);
			//ann.save("savedANN.ann");
			//ann =  EKFANN.load("savedANN.ann");
			//ann.setRNoise(.0001);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		for(int i = 0;i<8;i++){
			double[] out = ann.execute(in[i]);
			print(in[i]);
			println(out);
			print(new double[3]);
			double[] std = {Math.sqrt(ann.computeInnovCov(invar(in[i]), 0)),
							Math.sqrt(ann.computeInnovCov(invar(in[i]), 1)),
							Math.sqrt(ann.computeInnovCov(invar(in[i]), 2))};
			println(std);
			System.out.println();
		}
		System.out.println("Done");
	}
	
	
	static double[] invar(double[] input) {
		double[] r = new double[input.length];
		for(int k = 0;k<3;k++)
			r[k] = 0.1 * input[k];
		return r;
	}
	
	
	
	
	
	
	static void print(double[] ra){
		for(int i = 0;i<ra.length;i++){
			System.out.format("%.3f\t",ra[i]);
		}
		System.out.print("||\t");
	}
	
	static void println(double[] ra){
		for(int i = 0;i<ra.length;i++){
			System.out.format("%.3f\t",ra[i]);
		}
		System.out.println();
	}

}
