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

package edu.uconn.vstlf.data.doubleprecision;

public abstract class Function {
	
	/**
	 * Abstract method to be overridden by any subclass to implement a double function
	 * @param x
	 * @return
	 */
	public abstract double imageOf(double[] x) throws Exception;
	
	
	/**
	 * Performs this object's function on each element of the given series 
	 * in order to yield the image series.
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public Series imageOf(Series[] s) throws Exception{
		double[] array = new double[s[0].length()];
		double[] x = new double[s.length];
		for(int i = 0;i<s[0].length();i++){
			for(int j = 0;j<x.length;j++) x[j] = s[j].element(i+1);
			array[i] = imageOf(x);
		}
		return new Series(array,false);
	}
	
	public double imageOf(double x)throws Exception{
		double[] singleton = {x};
		return imageOf(singleton);
	}
	
	public Series imageOf(Series s) throws Exception{
		Series[] singleton = {s};
		return imageOf(singleton);
	}
	
	public double imageOf(double x0, double x1) throws Exception{
		double[] x = {x0,x1};
		return imageOf(x);
	}
	
	public Series imageOf(Series s0,Series s1) throws Exception{
		Series[] s = {s0,s1};
		return imageOf(s);
	}
	
	public double imageOf(double x0, double x1, double x2) throws Exception{
		double[] x = {x0,x1,x2};
		return imageOf(x);
	}
	
	public Series imageOf(Series s0,Series s1,Series s2) throws Exception{
		Series[] s = {s0,s1,s2};
		return imageOf(s);
	}
	
	public double imageOf(double x0, double x1, double x2, double x3) throws Exception{
		double[] x = {x0,x1,x2,x3};
		return imageOf(x);
	}
	
	public Series imageOf(Series s0,Series s1,Series s2, Series s3) throws Exception{
		Series[] s = {s0,s1,s2,s3};
		return imageOf(s);
	}
	
	public double imageOf(double x0, double x1, double x2, double x3, double x4) throws Exception{
		double[] x = {x0,x1,x2,x3,x4};
		return imageOf(x);
	}
	
	public Series imageOf(Series s0,Series s1,Series s2, Series s3,Series s4) throws Exception{
		Series[] s = {s0,s1,s2,s3,s4};
		return imageOf(s);
	}

}
