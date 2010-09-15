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

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.realtime.PCBuffer;
import edu.uconn.vstlf.realtime.VSTLF4SPoint;

public class Feed4sLoad implements Runnable{
	
	private Thread _feedThread;
	
	private PowerDB _db;
	private PCBuffer<VSTLF4SPoint> _output;
	private Calendar _cal;
	private Date _startDate, _endDate;
	private VSTLF4SPoint _endOfStream;
	private int _inc = 4;
	private String _inLoadType;
	
	public Feed4sLoad(PowerDB db, PCBuffer<VSTLF4SPoint> output, Date start, Date end,
			Calendar cal, VSTLF4SPoint endOfStream, String inLoadType) throws Exception {
		_db = db;
		_output = output;
		_cal = cal;
		_startDate = start;
		_endDate = end;
		_endOfStream = endOfStream;
		_inLoadType = inLoadType;
		
		if (start.getTime() % _inc*1000 != 0)
			throw new Exception("Start date " + start + " is not aligned properly");
		if (end.getTime() % _inc*1000 != 0)
			throw new Exception("End date " + end + " is not aligned properly");
	}
	
	public void init()
	{
		_feedThread = new Thread(this);
		_feedThread.start();
	}
	
	public void run()
	{
		try {

			Date prevTime = _startDate;
			Date at = _cal.addSecondsTo(prevTime, _inc);
			while (!at.after(_endDate)) {

				Series last4sLoad;
				// Get the loads in the last 4 seconds from the perst database
				last4sLoad = _db.getLoad(_inLoadType, prevTime, at);
				
				if (last4sLoad.length() == 0)
					System.err.println("No 4s load shows up from " + prevTime + " to " + at);
				
				// Integrate the loads in the last 4 seconds
				double accLoad = 0.0;
				for (int i = 1; i <= last4sLoad.length(); ++i)
					accLoad += last4sLoad.element(i);
				accLoad /= last4sLoad.length();
				
				// Feed it into the output buffer
				_output.produce(new VSTLF4SPoint(at, accLoad));
				
				prevTime = at;
				at = _cal.addSecondsTo(prevTime, _inc);
			}
			
			_output.produce(_endOfStream);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void join() throws InterruptedException
	{
		_feedThread.join();
	}
}
