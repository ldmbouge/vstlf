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

package edu.uconn.vstlf.database.perst;

import java.util.*;
import org.garret.perst.*;
public class PerstForecastSeries extends Persistent {
	String     _name;
    TimeSeries<PerstForecastPoint> _seq;
    
    @SuppressWarnings("unchecked")
	public PerstForecastSeries(String name,TimeSeries seq){
    	_name = name;
    	_seq = seq;
    }
    
    public void add(PerstForecastPoint p){
    	_seq.add(p);
    }
    
    public boolean has(Date t){
    	return _seq.has(t);
    }
    
    public PerstForecastPoint getTick(Date t){
    	return _seq.getTick(t);
    }
    
    public Iterator<PerstForecastPoint> iterator(){
    	return _seq.iterator();
    }
    
    public Iterator<PerstForecastPoint> iterator(Date st, Date ed){
    	Iterator<PerstForecastPoint> r = _seq.iterator(new Date(st.getTime()+1), ed);
    	return r;
    }
    
    public void remove(Date st, Date ed){
    	_seq.remove(new Date(st.getTime()+1), ed);
    }
    
    public Date first(){
    	return _seq.getFirstTime();
    }
    
    public Date last(){
    	return _seq.getLastTime();
    }

}
