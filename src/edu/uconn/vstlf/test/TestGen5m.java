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

import java.io.File;
import java.util.Date;

import org.junit.Test;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.PerstPowerDB;
import edu.uconn.vstlf.preprocessing.Feed4sLoad;
import edu.uconn.vstlf.preprocessing.From4STo5MLoad;
import edu.uconn.vstlf.preprocessing.Produce5MLoad;
import edu.uconn.vstlf.realtime.PCBuffer;
import edu.uconn.vstlf.realtime.VSTLF4SPoint;
import edu.uconn.vstlf.realtime.VSTLF5MPoint;
import edu.uconn.vstlf.shutil.RunTraining;

public class TestGen5m {
	private PCBuffer<VSTLF4SPoint> _4SBuf = new PCBuffer<VSTLF4SPoint>(100000);
	private PCBuffer<VSTLF5MPoint> _5MBuf = new PCBuffer<VSTLF5MPoint>(100000);
	private Calendar _cal = new Calendar();

	@Test public void testPrimitive() throws Exception
	{
		String indbName = "4s_Jun2008-9.pod";
		PowerDB indb = new PerstPowerDB(indbName,4);
		indb.open();

		Date st = RunTraining.parseDate("2009/01/01");
		Date ed = RunTraining.parseDate("2009/01/03");
		
		VSTLF4SPoint endOf4SStream = new VSTLF4SPoint(null, Double.NaN);
		VSTLF5MPoint endOf5MStream = new VSTLF5MPoint(null, Double.NaN, -1);
		
		Feed4sLoad feedThread = new Feed4sLoad(indb, _4SBuf, st, ed, _cal, endOf4SStream, "load");
		From4STo5MLoad aggThread = new From4STo5MLoad(_4SBuf, _5MBuf, st, _cal, endOf4SStream, endOf5MStream);
		feedThread.init();
		aggThread.init();
		
		// Wait for the feeding thread to end
		feedThread.join();
		
		// Wait for the aggregation thread to end
		aggThread.join();

		indb.close();
		VSTLF5MPoint p = _5MBuf.consume();
		assertEquals("Load", 14887.906666666666, p.getValue(), 1E-6);
		Date expectedDate = RunTraining.parseDate("2009/01/01 - 00:05:00");
		assertEquals("Date", expectedDate.getTime(), p.getStamp().getTime());
		
		p = _5MBuf.consume();
		assertEquals("Load", 14801.413333333334, p.getValue(), 1E-6);
		expectedDate = RunTraining.parseDate("2009/01/01 - 00:10:00");
		assertEquals("Date", expectedDate.getTime(), p.getStamp().getTime());
		
		p = _5MBuf.consume();
		assertEquals("Load", 14737.626666666667, p.getValue(), 1E-6);
		expectedDate = RunTraining.parseDate("2009/01/01 - 00:15:00");
		assertEquals("Date", expectedDate.getTime(), p.getStamp().getTime());
		
		p = _5MBuf.consume();
		assertEquals("Load", 14673.666666666666, p.getValue(), 1E-6);
		expectedDate = RunTraining.parseDate("2009/01/01 - 00:20:00");
		assertEquals("Date", expectedDate.getTime(), p.getStamp().getTime());
		
		p = _5MBuf.consume();
		assertEquals("Load", 14615.321600000001, p.getValue(), 1E-6);
		expectedDate = RunTraining.parseDate("2009/01/01 - 00:25:00");
		assertEquals("Date", expectedDate.getTime(), p.getStamp().getTime());
	}
	
	@Test public void testProduce4m() throws Exception
	{
		String indbName = "4s_Jun2008-9.pod", outdbName = "tstDb.pod";
		File f = new File(outdbName);
		f.deleteOnExit();
		Calendar cal =  new Calendar();
		Produce5MLoad producer = new Produce5MLoad(indbName, outdbName, cal, "load", new String[]{"filt"});
		Date st = RunTraining.parseDate("2009/01/01");
		Date ed = RunTraining.parseDate("2009/01/02");
		producer.execute(st, ed);
		
		PowerDB db = new PerstPowerDB(outdbName, 300);
		db.open();
		Date dbStrt = db.first("filt"), dbEnd = db.last("filt");
		assertEquals("Start date", dbStrt.getTime(), cal.addMinutesTo(st, 5).getTime());
		assertEquals("End date", dbEnd.getTime(), ed.getTime());
		
		Series sres = db.getLoad("filt", st, ed);
		for (int i = 1; i <= sres.length(); ++i) {
			Double val = new Double(sres.element(i));
			assertTrue(!val.equals(Double.NaN));
		}
			
	}
	/*public static void main(String[] args) throws Exception
	{
		TestGen5m tst= new TestGen5m();
		tst.Test();
	}*/
}
