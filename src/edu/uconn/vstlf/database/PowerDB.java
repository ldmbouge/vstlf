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

package edu.uconn.vstlf.database;

import java.util.Collection;
import java.util.Date;

import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.database.xml.LoadData;
public abstract class PowerDB {
	
	public abstract void open();
	
	public abstract void close();
	
	public abstract void commit();
	
	public abstract Series getLoad(String s, Date st,Date ed)throws Exception;
	
	public abstract double getLoad(String s, Date t) throws Exception;
		
	public abstract void removeBefore(String s, Date t);
	
	public abstract void addLoad(String s, Date time, double load);
	
	public abstract void startTransaction();
	
	public abstract void endTransaction();
	
	public abstract void addLoadNL(String s, Date time,double load);
	
	public abstract void fill(String s, Collection<LoadData> set);
	
	public abstract Date first(String s);
	
	public abstract Date begin(String s);
	
	public abstract Date last(String s);
	
	public abstract Series getForecast(Date t) throws Exception;
	
	public abstract double[] getForecastArray(Date t) throws Exception;
	
	public abstract void addForecast(Date time, Series forecast) throws Exception;
	
	public abstract void addForecastArray(Date time, double[] forecast) throws Exception;
	
	public abstract void addStats(Date ed, int nb, double[] sum, double[] sumP, double[] sos, double[] ovr, double[] und);
	
	public abstract int getNb();
	
	public abstract Series getSum() throws Exception;
	
	public abstract Series getSumP() throws Exception;
	
	public abstract Series getSumOfSquares() throws Exception;

	public abstract Series getMaxOvr() throws Exception;
	
	public abstract Series getMaxUnd() throws Exception;
}
