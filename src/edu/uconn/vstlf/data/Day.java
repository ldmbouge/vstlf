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

package edu.uconn.vstlf.data;

public class Day implements Comparable<Day>{
	int _yr, _mo, _dy;
	
	public Day(int yr, int mo, int dy){_yr = yr; _mo = mo; _dy = dy;}
	
	public int compareTo(Day d){
		if(_yr<d._yr) return -1; if(_yr>d._yr) return 1;
		if(_mo<d._mo) return -1; if(_mo>d._mo) return 1;
		if(_dy<d._dy) return -1; if(_dy>d._dy) return 1;
		return 0;
	}
	public int hashCode() {
		return (_yr - 1900) * 101 * 12 + _mo * 12 + _dy;
	}
	public boolean equals(Object od){
		assert false : "equals should not be called on Day class";
		if (od instanceof Day) {
			Day d = (Day)od;
			return _yr == d._yr && _mo == d._mo && _dy == d._dy;
		} else return false;
	}
	
	public boolean before(Day d){
		if (_yr < d._yr) return true;
		if (_yr > d._yr) return false;
		if (_mo < d._mo) return true;
		if (_mo > d._mo) return false;
		if (_dy < d._dy) return true;
		return false;
	}
	
	public boolean after(Day d) {return compareTo(d)==1;}
	
	public int year(){
		return _yr;
	}
	
	public int month(){
		return _mo;
	}
	
	public int day(){
		return _dy;
	}
	
	public String toString() {return _yr +" - " + _mo + " - " + _dy;}

}
