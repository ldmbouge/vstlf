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

package edu.uconn.vstlf.database.xml;

import java.util.*;

public class LoadData implements Comparable<LoadData>
{
	Date date;
	double val;
	boolean quality;
	public LoadData(double in, boolean quality, Date d)
	{
		this.date = d;
		this.val = in;
		this.quality = quality;
	}
	public LoadData()
	{

	}
	public void setDate(Date in)
	{
		this.date = in;
	}
	public Date getDate()
	{
		return this.date;
	}
	public void setValue(double in)
	{
		this.val = in;
	}
	public double getValue()
	{
		return this.val;
	}
	public void setQuality(boolean in)
	{
		this.quality = in;
	}
	public boolean getQuality()
	{
		return this.quality;
	}
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Date:  " + this.date.toString() + ", value = " + this.val + ", quality = " + this.quality);
		return buf.toString();
	}
	
	public int compareTo(LoadData d){
		if(date.before(d.date)) return -1;
		if(date.equals(d.date)) return 0;
		return 1;
	}

}