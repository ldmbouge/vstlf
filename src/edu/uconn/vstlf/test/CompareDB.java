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

import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class CompareDB {
	@Test public void Test() throws Exception
	{
		Calendar cal = new Calendar();
		PerstPowerDB db1 = new PerstPowerDB("4sdb.pod", 4);
		db1.open();
		
		PerstPowerDB db2 = new PerstPowerDB("4sdatabase.pod", 4);
		db2.open();
		
		String loadType = "raw";
		Date ds1 = db1.first(loadType), de1 = db1.last(loadType);
		Date ds2 = db2.first(loadType), de2 = db2.last(loadType);
		assertEquals("Start date", ds1.getTime(), ds2.getTime());
		assertEquals("End date", de1.getTime(), de2.getTime());
		Series load1 = db1.getLoad(loadType, cal.addSecondsTo(ds1, -4), cal.addDaysTo(ds1, 20));
		Series load2 = db2.getLoad(loadType, cal.addSecondsTo(ds2, -4), cal.addDaysTo(ds2, 20));
		assertEquals("load size", load1.length(), load2.length());
		for (int i = 1; i <= load1.length(); ++i)
			assertEquals("load "+i, load1.element(i), load2.element(i), 1E-6);
		
		load1 = db1.getLoad(loadType, cal.addDaysTo(de1, -20), de1);
		load2 = db2.getLoad(loadType, cal.addDaysTo(de2, -20), de2);
		for (int i = 1; i <= load1.length(); ++i)
			assertEquals("load "+i, load1.element(i), load2.element(i), 1E-6);
		db1.close();
		db2.close();
	}
	
	@Test public void Test1() throws Exception
	{
		Calendar cal = new Calendar();
		PerstPowerDB db1 = new PerstPowerDB("5mdb.pod", 300);
		db1.open();
		
		PerstPowerDB db2 = new PerstPowerDB("5mdatabase.pod", 300);
		db2.open();
		
		String loadType = "filt";
		Date ds1 = db1.first(loadType), de1 = db1.last(loadType);
		Date ds2 = db2.first(loadType), de2 = db2.last(loadType);
		assertEquals("Start date", ds1.getTime(), ds2.getTime());
		assertEquals("End date", de1.getTime(), de2.getTime());
		Series load1 = db1.getLoad(loadType, cal.addSecondsTo(ds1, -300), de1);
		Series load2 = db2.getLoad(loadType, cal.addSecondsTo(ds2, -300), de2);
		assertEquals("load size", load1.length(), load2.length());
		for (int i = 1; i <= load1.length(); ++i)
			assertEquals("load "+i, load1.element(i), load2.element(i), 1E-6);
		
		db1.close();
		db2.close();
	}
}
