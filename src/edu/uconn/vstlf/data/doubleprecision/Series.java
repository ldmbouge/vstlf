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

public class Series {
	private double[] _array;
	
	/**
	 * Creates a series to hold the data in the given array.
	 * @param array The given array.
	 * @param cpy Specifies whether the series should make a copy of the array
	 * @throws Exception
	 */
	public Series(double[] array, boolean cpy) throws Exception{
		if(array == null) throw new Exception("Cannot create a series from no array.");
		if(cpy){
			_array = new double[array.length];
			System.arraycopy(array, 0, _array, 0, array.length);
		}
		else{
			_array = array;
		}
	}
	
	/**
	 * Creates a series to hold the data in the given array.  Makes a deep copy of the array.
	 * @param array The given array.
	 * @throws Exception
	 */
	public Series(double[] array)throws Exception{
		this(array,true);
	}
	
	public Series() throws Exception{
		this(new double[0]);
	}
	
	public Series(int len) throws Exception{
		this(new double[len],false);
	}
	
	/**
	 * Returns the length of this series.
	 */
	public int length(){
		return _array.length;
	}
	
	/**
	 * Returns the ith element of this series.  If i does not appear in the 
	 * range of indices corresponding to the data stored for this series,
	 * then the ith value is extrapolated using an axial reflection.
	 * @param i
	 * @throws Exception
	 */
	/*
	public double element(int i)throws Exception{
		try{
			if(i<1) 
				return _array[0-i];
			int n = _array.length;
			if(i > n) 
				return _array[(n)-(i-n)];
			return _array[i-1];
		}
		catch(Exception e){
			throw new Exception("Series has no "+i+"th element.  ");
		}
	}
	*/
	
	
	public double element(int i)throws Exception{
		try{return _array[i-1];}
		catch(Exception e){
			if(i<1) return _array[0-i]; //return 2*_array[0]-(_array[1-i]);
			int n = _array.length;
			if(i>n) return _array[(n)-(i-n)];//return 2*_array[n-1]-(_array[(n-1)-(i-n)]);
			throw new Exception("Series has no "+i+"th element.  ");
		}
	}
	
	
	public double elementM(int i)throws Exception{
		try{return _array[i-1];}
		catch(Exception e){
			if(i<1) return 2*_array[0]-(_array[1-i]);
			int n = _array.length;
			if(i>n) return 2*_array[n-1]-(_array[(n-1)-(i-n)]);
			throw new Exception("Series has no "+i+"th element.  ");
		}
	}
	
	
	
	
	
	
	/**
	 * Returns the ith element of this series.  If i does not appear in the 
	 * range of indices corresponding to the data stored for this series,
	 * then the ith value is extrapolated using a central reflection.
	 * @param i
	 * @return
	 * @throws Exception
	 */
	/*
	public double elementM(int i)throws Exception{
		try{
			if(i<1) 
				return 2*_array[0]-(_array[1-i]);
			int n = _array.length;
			if(i > n) 
				return 2*_array[n-1]-(_array[(n-1)-(i-n)]);
			return _array[i-1];
		}
		catch(Exception e){
			throw new Exception("Series has no "+i+"th element.  ");
		}
	}
	*/
	
	/**
	 * Returns the array of values in this Series.  If cpy is true then it makes a copy.  
	 * Otherwise, a pointer to the array in the series is returned.
	 * @param cpy
	 */
	public double[] array(boolean cpy){
		if(cpy){
			double[] array = new double[_array.length];
			System.arraycopy(_array, 0, array, 0, array.length);
			return array;
		}
		return _array;
	}
	
	/**
	 * Returns an array containing the values in this series.
	 */
	public double[] array(){
		return array(true);
	}
	
	/**
	 * Returns the segment of this series beginning with the ith element and ending with the jth.  
	 * @param i
	 * @param j
	 * @return
	 * @throws Exception
	 */
	public Series subseries(int i, int j)throws Exception{
		if(i>j) throw new Exception("Series segment has negative length.  ");
		double[] array = new double[j - i + 1];
		int n=0;
		for(int k=i;k<=j;k++)array[n++] = element(k);
		return new Series(array,false);
	}
	
	public Series prefix(int len)throws Exception{
		return subseries(1,len);
	}
	
	public Series suffix(int len)throws Exception{
		return subseries(_array.length-len+1,_array.length);
	}
	
	/**
	 * Returns the result of appending the given series to the end of this series.
	 * @param s The given series
	 * @return A new Series object
	 * @throws Exception
	 */
	public Series append(Series s) throws Exception{
		double[] array = new double[_array.length + s._array.length];
		System.arraycopy(_array, 0, array, 0, _array.length);
		System.arraycopy(s._array,0,array,_array.length,s._array.length);
		return new Series(array,false);
	}
	
	/**
	 * Returns a series that is this series in reverse order.
	 */
	public Series reverse()throws Exception{
		double[] array = new double[_array.length];
		for(int i=0,j=array.length;i<array.length;i++)array[i] = _array[--j];
		return new Series(array,false);
	}
	
	public int countOf(double val)throws Exception{
		int r = 0;
		Double v = new Double(val);
		for(int i=0; i<length();i++) if(v.equals(_array[i])) r++;
		return r;
	}
	
	/** 
	 * Returns the componentwise sum of this series and the given series.
	 * @param s
	 * @throws Exception
	 */
	public Series plus(Series s) throws Exception{
		if(length()!=s.length()) throw new Exception("Cannot add series of different lengths");
		double[] array = new double[length()];
		for(int i = 0;i<array.length;i++) array[i] = _array[i] + s._array[i];
		return new Series(array,false);
	}
	
	/**
	 * Returns the componentwise difference of this series and the given series
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public Series minus(Series s) throws Exception{
		if(length()!=s.length()) throw new Exception("Cannot compare series of different lengths");
		double[] array = new double[length()];
		for(int i = 0;i<array.length;i++) array[i] = _array[i] - s._array[i];
		return new Series(array,false);
	}
	
	/**
	 * Returns a new series, containing each element of this series multiplied by f.
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public Series times(double f) throws Exception{
		double[] array = new double[length()];
		for(int i = 0;i<array.length;i++) array[i] = _array[i] * f;
		return new Series(array,false);
	}
	
	/**
	 * Returns a new series one (mod)th the length of this series, in which the ith element is the
	 * average over the ith block of (mod) elements in this series.
	 * @param mod
	 * @return
	 * @throws Exception
	 */
	public Series integrate(int mod)throws Exception{
		if(_array.length%mod!=0) throw new Exception("Series length is not a multiple of "+mod+".  ");
		double[] array = new double[_array.length/mod];
		int j = 0;
		for(int i=0;i<array.length;){
			do{
				array[i]+=_array[j++];
				}while(j<(i+1)*mod);
			array[i++]/=mod;
		}
		return new Series(array,false);
	}
	
	
	public Series differentiate()throws Exception{
		double[] array = new double[_array.length];
		for(int i = 0;i<array.length;i++) array[i] = (element(i+1) - element(i))/element(i);
		return new Series(array,false);
	}
	
	
	public Series undifferentiate(double prev)throws Exception{
		double[] array = new double[_array.length];
		array[0] = _array[0] * prev + prev; 
		for(int i = 1;i<array.length;i++) {
			array[i] = _array[i] * array[i-1] + array[i-1];
			//System.err.println(array[i]+" = "+_array[i]+" * "+ _array[i-1]+" + "+_array[i-1]);
		}
		return new Series(array,false);
	}
	
	
	
	/**
	 * Normalizes the values in this series from the initial interval to the final interval.
	 * @param lowi The lower bound of the initial interval.
	 * @param upi The initial upper bound.
	 * @param lowf The final lower bound.
	 * @param upf The final upper bound.
	 * @return
	 * @throws Exception
	 */
	public Series normalize(double lowi,double upi,double lowf,double upf)throws Exception{
		double[] array = new double[_array.length];
		for(int i=0;i<array.length;i++)array[i] = lowf +(upf-lowf)*((_array[i]-lowi)/(upi-lowi));
		return new Series(array,false);
	}
	
	public double meanOfSquares(){
		double sum = 0;
		for(int i = 0;i<_array.length;i++) sum += _array[i]*_array[i];
		return sum/_array.length;
	}
	
	/**
	 * Returns a new series produced from this series using a zero-shift
	 * moving average of length w over this series.  The extrapolation performed 
	 * by the 'element' method is used to reduce end effects.
	 * @param w
	 * @return
	 * @throws Exception
	 */
	public Series lowPass(int r)throws Exception{
		double[] x = new double[2*r+1];
		Function mf = new MeanFunction();
		double[] array = new double[_array.length];
		for(int i=1;i<=array.length;i++){
			int k=0;
			for(int j=i-r;j<=i+r;j++)x[k++]=elementM(j);
			array[i-1] = mf.imageOf(x);
		}
		return new Series(array,false);
	}
	
	/**
	 * Returns a new series produced from this series using a "shifty"
	 * moving average of length w over this series.  The extrapolation performed 
	 * by the 'element' method is used to reduce end effects.
	 * @param w
	 * @return
	 * @throws Exception
	 */
	public Series lowPassWithShift(int w)throws Exception{
		double[] x = new double[w];
		Function mf = new MeanFunction();
		double[] array = new double[_array.length];
		for(int i=1;i<=array.length;i++){
			int k=0;
			for(int j=i-w+1;j<=i;j++)x[k++]=elementM(j);
			array[i-1] = mf.imageOf(x);
		}
		return new Series(array,false);
	}
	
	/**
	 * Returns a new series produced from this series through a
	 * forward and reverse application of the "shifty" lowpass filter method.
	 * Uses filter length of w.
	 * Results in a net shift of zero.
	 * @param w
	 * @return
	 * @throws Exception
	 */
	public Series lowPassFR(int w)throws Exception{
		return lowPassWithShift(w).reverse().lowPassWithShift(w).reverse();
	}
	
	public Series stupidPatchSpikesLargerThan(double t)throws Exception{
		double[] array = new double[_array.length];
		//Series diff = minus(lowPassFR(w));
		for(int i=1;i<=length();i++){
			double tt = t;
			if(Math.abs(element(i)-element(i-1))>t){//(Math.abs(diff._array[i])>t){
				int st = i-1,ed=i+1;
				while(Math.abs(element(i)-element(st))>tt){
					tt+=t;
					ed = ++i;
				}
					
				for(int j=st+1,k=1;j<=ed;j++,k++){
					array[j-1] = element(st)+ k*(element(ed)-element(st))/(ed-st);
				}
					
			}
			else{
				array[i-1] = element(i);
			}
		}
		return new Series(array,false);
	}
	
	
	public Series patchSpikesLargerThan(double t,int w)throws Exception{
		double[] array = new double[_array.length]; 
		Series diff = minus(lowPassFR(w));
		for(int i=1;i<=length();i++){
			if(Math.abs(diff.element(i))>t){
				int st = i-1,ed=i+1;
				while(Math.abs(diff.element(i))>t) if(++i <= length()) ed = i; else break ;
				if(ed>length()) ed = length();
				if(ed==length()) array[ed-1] = element(st) + (ed-st)*(element(st)-element(st-1));
				else array[ed-1] = element(ed);
				for(int j=st+1,k=1;j<=ed;j++,k++)
					array[j-1] = element(st)+ k*(array[ed-1]-element(st))/(ed-st);	
			}
			else{
				array[i-1] = element(i);
			}
		}
		return new Series(array,false);
	}
	
	public Series otherPatchSpikesLargerThan(double t, int w)throws Exception{
		Series ol = this;
		Series nw = new Series(ol.length()); 
		Series diff = minus(lowPassFR(w));
		System.out.println("diff\t"+diff);
		for(int i=1;i<=length();i++){
			if(Math.abs(diff.element(i))>t){
				int st = i,ed=i+1;
				System.out.println("st: "+st);
				while(Math.abs(diff.element(i))>t) 
					if(++i < length()) 
						ed = i; 
					else 
						break ;
				System.out.println("ed: "+ed);
				if(ed==length()) 
					nw.set(ed, ol.element(st) + (ed-st)*(ol.element(st)-ol.element(st-1)));
				else 
					nw.set(ed, ol.element(ed));
				for(int j=st+1,k=1;j<=ed;j++,k++)
					nw.set(j, ol.element(st)+ k*(nw.element(ed)-ol.element(st))/(ed-st));
			}
			else{
				nw.set(i, ol.element(i));
			}
		}
		return nw;
	}
	
	public Series patchSpikesLargerThan(double t)throws Exception{
		Series nw = new Series(length());
		System.arraycopy(this._array, 0, nw._array, 0, this._array.length);
		double inc, abs, recdir, maxInc = 2*t;
		
		//nw.set(1, element(1));		// i forgot to copy the first element
		for(int i=2;i<=length();i++){		//loop through the series
			inc = nw.element(i) - nw.element(i-1);
			abs = Math.abs(inc);
			if(abs > maxInc){				//if a spike begins
				recdir = -(inc%(abs-1));			//direction of recovery increment
				int st = i-1, ed = i;			//st and ed should be the indices just outside the gap
				do{								//fast forward until signal shoots back toward recovery
					i++;
					inc = nw.element(i) - nw.element(i-1);
					if(i > length())
						break;
				}while(recdir*inc < maxInc);	//int mid = i;System.out.println(recdir);
				do{								//fast forward until signal has stopped shooting back
					inc = nw.element(i+1) - nw.element(i);
					if(recdir*inc < maxInc)
						break;
					i++;
				}while(i<=length());
				if(i>length()){					//if it never shot back, extrapolate from st
					ed = length();
					nw.set(ed, nw.element(st) + (ed-st)*(nw.element(st)-nw.element(st-1)));
					//System.err.println("off");
				}
				else{							//otherwise, interpolate between st and ed
					ed = i;
					//nw.set(ed, nw.element(ed));
				}//System.out.format("%d\n%d\n%d\n", st,mid,ed);
				//System.out.println(nw);
				for(int j=st+1,k=1;j<=ed;j++,k++)
					nw.set(j, nw.element(st)+ k*(nw.element(ed)-nw.element(st))/(ed-st));
			}
			/*else{						//if no spike, just copy
				nw.set(i, element(i));
			}*/
		}
		
		return nw;
	}
	
	
	void set(int i, double v){
		_array[i-1] = v;
	}

	public Series convolve(double[] filt)throws Exception{
		int ext = filt.length-1;
		Series orig = this.padZ(ext, ext);
		double[] array = new double[orig._array.length];
		for(int i = 1;i<=array.length;i++)
			for(int j = 0;j<filt.length;j++)
				array[i-1] += orig.element(i+j)*filt[j];
		return new Series(array,false).pad(0, -ext);
	}
	
	public Series pad(int front, int back)throws Exception{
		return subseries(1-front,length()+back);
	}
	
	public Series padZ(int front, int back)throws Exception{
		double[] array = new double[_array.length+front+back];
		System.arraycopy(_array, 0, array, front, _array.length);
		return new Series(array,false);
	}
	
	public Series downSample()throws Exception{
		double[] array = new double[_array.length>>1];
		for(int j =0;2*j+1<_array.length;j++) array[j] = _array[2*j+1];
		return new Series(array,false);
	}
	
	public Series upSample()throws Exception{
		int length = (_array.length*2)-1;//(_array.length%2);
		double[] array = new double[length];
		for(int i = 0;i<_array.length;i++) array[2*i] = _array[i];
		return new Series(array,false);
	}
	
	public Series[] daub4Separation(int lvls, double[] loD, double[] hiD, double[] loR, double[] hiR)throws Exception{
		Series[] components = new Series[lvls+1];
		Series orig = this;
		components[0] = orig;
		for(int lvl = 0;lvl<lvls;lvl++){//iteratively apply the d4 filter bank
			Series det = orig.pad(hiD.length-1,hiD.length-1)
							 .convolve(hiD)
							 .pad(-(hiD.length-1), -(hiD.length-1))
							 .downSample();
			Series app = orig.pad(loD.length-1,loD.length-1)
							 .convolve(loD)
							 .pad(-(loD.length-1), -(loD.length-1))
							 .downSample();
			components[lvl] = det;			//storing every set of detail coefficients
			orig = components[lvl+1] = app;	//and keeping the smallest set of approximation coef
		}
		for(int lvl = lvls-1;lvl>=0;lvl--){//invert each of the filtering and downsampling operations
			for(int k = lvl;k<=lvls;k++){
				int len = (k == 0)? length():components[k-1].length();
				if(k==lvl){
					components[k] = components[k].upSample().convolve(hiR);
				}
				else{
					components[k] = components[k].upSample().convolve(loR);
				}
				int strip = components[k].length() - len, stripL = strip/2, stripR = strip - stripL;
				components[k] = components[k].pad(-stripL, -stripR);
			}
		}
		return components;
	}
		
	public String toString(){
		java.text.DecimalFormat df = new java.text.DecimalFormat();
		df.setMinimumFractionDigits(4); df.setMaximumFractionDigits(4); 
		df.setMinimumIntegerDigits(5); df.setMaximumIntegerDigits(5); 
		df.setPositivePrefix("+");
		df.setGroupingUsed(false);
		StringBuffer sb = new StringBuffer();
		try{
			for(int i=1;i<length();i++){sb.append(df.format(element(i))); sb.append("\t");}
			sb.append(df.format(element(length())));
		}catch(Exception e){}
		return sb.toString();
	}
}
