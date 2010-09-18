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

package edu.uconn.vstlf.preprocessing;

import java.util.Date;
import java.util.logging.Level;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.PerstPowerDB;
import edu.uconn.vstlf.realtime.PCBuffer;
import edu.uconn.vstlf.realtime.VSTLF4SPoint;
import edu.uconn.vstlf.realtime.VSTLF5MPoint;

public class Produce5MLoad {
	private String _pst4SDBName, _pst5MDBName;
	private Calendar _cal;
	private PCBuffer<VSTLF4SPoint> _4SBuf = new PCBuffer<VSTLF4SPoint>(200);
	private PCBuffer<VSTLF5MPoint> _5MBuf = new PCBuffer<VSTLF5MPoint>(200);
	private String _inLoadType;
	private String [] _outLoadType;

	public Produce5MLoad(String pst4SDBName, String pst5MDBName, Calendar cal,
			String inLoadType, String[] outLoadType)
	{
		_pst4SDBName = pst4SDBName;
		_pst5MDBName = pst5MDBName;
		
		_cal = cal;
		_outLoadType = outLoadType;
		_inLoadType = inLoadType;
	}
	
	public void execute(Date st, Date ed) throws Exception
	{
		PowerDB outdb = new PerstPowerDB(_pst5MDBName,300);
		PowerDB indb = new PerstPowerDB(_pst4SDBName,4);

		indb.open();
		outdb.open();
		
		executeImpl(indb, outdb, st, ed);
		
		indb.close();
		outdb.close();
	}
	
	private void executeImpl(PowerDB indb, PowerDB outdb, Date st, Date ed) throws Exception
	{
		String methodName = Produce5MLoad.class.getDeclaredMethod("executeImpl", 
				new Class[]{PowerDB.class, PowerDB.class, Date.class, Date.class}).getName();
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, Produce5MLoad.class.getName(),
				methodName, "Start transforming the 4s loads into 5m loads from " + st + " to " + ed));
		
		VSTLF4SPoint endOf4SStream = new VSTLF4SPoint(null, Double.NaN);
		VSTLF5MPoint endOf5MStream = new VSTLF5MPoint(null, Double.NaN, -1);

		Feed4sLoad feedThread = new Feed4sLoad(indb, _4SBuf, st, ed, _cal, endOf4SStream, _inLoadType);
		From4STo5MLoad aggThread = new From4STo5MLoad(_4SBuf, _5MBuf, st, _cal, endOf4SStream, endOf5MStream);
		Store5MLoad outThread = new Store5MLoad(_5MBuf, outdb, _outLoadType, endOf5MStream);
		feedThread.init();
		aggThread.init();
		outThread.init();
		
		// Wait for the feeding thread to end
		feedThread.join();
		
		// Wait for the aggregation thread to end
		aggThread.join();

		// Wait for the storing thread to end
		outThread.join();
		
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, Produce5MLoad.class.getName(),
				methodName, "Loads from " + st + " to " + ed + " is aggregated into 5m loads"));
	}
	
	public void execute() throws Exception
	{
		PowerDB outdb = new PerstPowerDB(_pst5MDBName,300);
		PowerDB indb = new PerstPowerDB(_pst4SDBName,4);
		indb.open();
		outdb.open();
		
		Date st = indb.first(_inLoadType);
		Date ed = indb.last(_inLoadType);
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, Produce5MLoad.class.getName(),
				"execute", String.format("Converting 4s signal between %s - %s\n", st,ed)));

		executeImpl(indb, outdb, st, ed);
		outdb.close();
		indb.close();
	}
}
