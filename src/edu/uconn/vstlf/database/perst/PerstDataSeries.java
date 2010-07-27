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
import org.garret.perst.TimeSeries.Tick;
public class PerstDataSeries extends Persistent {
	String     _name;
    TimeSeries<Tick> _seq;
    public PerstDataSeries() {
    	_name = null;
    	_seq = null;
    }
    public PerstDataSeries(String name,TimeSeries<Tick> seq){
    	_name = name;
    	_seq = seq;
    }
    
    public void add(Tick p){
    	_seq.add(p);
    }
    
    public void add(Collection<PerstDataPoint> set){
    	_seq.addAll(set);
    }
    
    public boolean has(Date t){
    	return _seq.has(t);
    }
    
    public Tick getTick(Date t){
    	return _seq.getTick(t);
    }
    
    public Iterator<Tick> iterator(){
    	return _seq.iterator();
    }
    
    public Iterator<Tick> iterator(Date st, Date ed){
    	Iterator<Tick> r = _seq.iterator(st, ed);
    	return r;
    }
    
    public void remove(Date st, Date ed){
    	_seq.remove(st, ed);
    }
    
    public Date first(){
    	return _seq.getFirstTime();
    }
    
    public Date last(){
    	return _seq.getLastTime();
    }
    public int getSize() {
    	return _seq.size();
    }
	public boolean equals(Object o) {
		if (o instanceof PerstDataSeries) {
			PerstDataSeries pb = (PerstDataSeries)o;
			if (!_name.equals(pb._name)) return false;
			boolean eq = _seq.countTicks() == pb._seq.countTicks();
			Iterator<Tick> i1 = _seq.iterator();
			Iterator<Tick> i2 = pb._seq.iterator();
			while (eq && i1.hasNext() && i2.hasNext()) {
				Tick t1 = i1.next();
				Tick t2 = i2.next();
				eq = t1.equals(t2);				
			}
			eq = eq && (i1.hasNext() == i2.hasNext());
			return eq;
		} else return false;
	}
	public int hashCode() { return _seq.size();}
}
