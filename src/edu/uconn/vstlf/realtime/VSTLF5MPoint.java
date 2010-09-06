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

package edu.uconn.vstlf.realtime;
import java.util.Date;

import edu.uconn.vstlf.data.Message.VSTLFMessage;


public class VSTLF5MPoint extends VSTLFObservationPoint {
	public static VSTLFMessage.Type mtype = VSTLFMessage.Type.RT5mPoint; 

	int _nbObs;
	public VSTLF5MPoint() {
		super(mtype);
	}
	public VSTLF5MPoint(Date at,double val, int nbObs) {
		super(mtype, at,val);
		_nbObs = nbObs;
	}
	public String toString() { return "5 minute point:" + _at.toString() + " value=" + Double.toString(_val);}
	public Date getStamp()   { return _at;}
	public double getValue() { return _val;}
	public int getNumObs()	 { return _nbObs;}
 }
