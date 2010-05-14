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

package edu.uconn.vstlf.test;
import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.*;

import edu.uconn.vstlf.data.doubleprecision.Series;

public class NaNEquality {
	@Test public void equal() throws Exception
	{
		double [] nanArray = new double[3];
		nanArray[0] = Double.NaN;
		nanArray[2] = Double.NaN;
		Series s = new Series(nanArray);
		assertTrue(s.countOf(Double.NaN) == 2);
		double d = Double.NaN;
		Double dc = new Double(d);
		assertTrue(dc.equals(Double.NaN));
	}
	
	public static void main(String[] args)
	{
		JUnitCore.runClasses(NaNEquality.class);
	}
}
