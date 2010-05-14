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

import java.util.Date;

import org.junit.*;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.perst.PerstPowerDB;
import edu.uconn.vstlf.shutil.RunFromXMLToPerst;

public class PersetGetLoadTest {
	@Test public void Test() throws Exception
	{
		PerstPowerDB db = new PerstPowerDB("5m_2007-Mar2009.pod", 300);
		db.open();
		Calendar cal = new Calendar("America/New_York");
		Date s = cal.newDate(2008, 0, 1, 0, 0, 0);
		Date t = cal.newDate(2008, 0, 2, 0, 0, 0);
		db.getLoad("filt", s, t);
	}
	
	/*
	@Test public void Test1() throws Exception
	{
		PerstPowerDB db = new PerstPowerDB("4sdb.pod", 4);
		db.open();
		Calendar cal = new Calendar("America/New_York");
		Date s = cal.newDate(2009, 7, 1, 0, 0, 0);
		Date t = cal.newDate(2009, 7, 1, 0, 0, 8);
		Series re = db.getLoad("raw", s, t);
		Double res = new Double(re.element(1));
		assertTrue(!res.equals(Double.NaN));
		
		assertTrue(RunFromXMLToPerst.checkIntegrity(db, "raw", 4));
		db.close();
	}
	
	
	@Test public void Test2() throws Exception
	{
		PerstPowerDB db = new PerstPowerDB("iso_2009_5m.pod", 300);
		db.open();
		
		assertTrue(RunFromXMLToPerst.checkIntegrity(db, "raw", 300));
		db.close();
	}
	*/
	
	@Test public void Test3() throws Exception
	{
		PerstPowerDB db = new PerstPowerDB("5mdb1.pod", 300);
		db.open();
		
		assertTrue(IntegrityTest.checkIntegrity(db, "filt", 300));
		assertTrue(IntegrityTest.checkIntegrity(db, "load", 300));
		db.close();
	}
	
	@Test public void TestChe() throws Exception
	{
		PerstPowerDB db = new PerstPowerDB("5mche.pod", 300);
		db.open();
		
		assertTrue(IntegrityTest.checkIntegrity(db, "filt", 300));
		assertTrue(IntegrityTest.checkIntegrity(db, "load", 300));
		db.close();
	}

}
