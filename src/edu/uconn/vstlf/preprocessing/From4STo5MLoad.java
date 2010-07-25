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
import java.util.LinkedList;
import java.util.Vector;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.doubleprecision.ThresholdChoiceFunction;
import edu.uconn.vstlf.realtime.PCBuffer;
import edu.uconn.vstlf.realtime.VSTLF4SPoint;
import edu.uconn.vstlf.realtime.VSTLF5MPoint;
import java.util.logging.*;

class AggResult
{
	private double _val;
	private int _size;
	
	public AggResult(double val, int size)
	{
		_val = val;
		_size = size;
	}
	
	public double Value() { return _val; }
	public int Size() { return _size; }
}


public class From4STo5MLoad implements Runnable{
    private Logger _logger = Logger.getLogger("From4Sto5MLoad");
    
	private PCBuffer<VSTLF4SPoint> _input;
	private PCBuffer<VSTLF5MPoint> _output;
	private Date _startDate;
	private Calendar _cal;
	private VSTLF4SPoint _eois;
	private VSTLF5MPoint _eoos;
	
	private Thread _feedThread;
	private int _inc = 300;
	
	private double _filterThreshold = 50;

	
	public void init()
	{
		_feedThread = new Thread(this);
		_feedThread.start();
	}
	
	public From4STo5MLoad(PCBuffer<VSTLF4SPoint> input, PCBuffer<VSTLF5MPoint> output,
			Date startDate, Calendar cal,
			VSTLF4SPoint endOfInStream, VSTLF5MPoint endOfOutStream) throws Exception
	{
		_input = input;
		_output = output;
		_startDate = startDate;
		_cal = cal;
		_eois = endOfInStream;
		_eoos = endOfOutStream;
		
		if (startDate.getTime() % _inc*1000 != 0)
			throw new Exception("Start date " + startDate + " is not aligned properly");

		_logger.addHandler(new FileHandler("preprocessing.log"));
	}
	
	public void run()
	{
		Date prevTime = _startDate;
		Date nextTime = _cal.addSecondsTo(prevTime, _inc);
		Date prevPointTime = _startDate;
		LinkedList<VSTLF4SPoint> fourSecLoads = new LinkedList<VSTLF4SPoint>();
		while (true) {
			VSTLF4SPoint p = _input.consume();
			
			// End of the stream has been reached, exit the thread
			if (p == _eois) {
				_output.produce(_eoos);
				break;
			}
				
			// Check load and at it to the queue
			if (p.getStamp().before(prevPointTime))
				_logger.warning("4s load at " + p.getStamp() + " is out of order (before loat at " + fourSecLoads.getLast().getStamp() + ")");
			else {
				fourSecLoads.addLast(p);
				prevPointTime = p.getStamp();
			}

			// Shift time window and aggregate 5m loads
			while (fourSecLoads.size() != 0 &&
					!fourSecLoads.getLast().getStamp().before(nextTime)) {
				// Ignore any loads that are before the start of the time window
				while (fourSecLoads.size() != 0 &&
						!fourSecLoads.getFirst().getStamp().after(prevTime)) {
					_logger.warning("4s load at " + fourSecLoads.getFirst().getStamp() +
							" is ignored, before time window (" + prevTime + ", " + nextTime +"]");
					fourSecLoads.removeFirst();
				}
				
				// Find all the loads needed to aggregate the value on 5M boundary
				Vector<Double> window = new Vector<Double>();
				while (fourSecLoads.size() != 0 &&
						!fourSecLoads.getFirst().getStamp().after(nextTime)) {
					window.add(fourSecLoads.getFirst().getValue());
					fourSecLoads.removeFirst();
				}
				
				// aggregate the 4s points into a 5m points
				AggResult result = aggregate(window, _filterThreshold);
				double aggVal = result.Value();
				int filtSz = result.Size();
				Double dc = new Double(aggVal);
				if (!dc.equals(Double.NaN)) {
					_output.produce(new VSTLF5MPoint(nextTime, aggVal ,filtSz)); //into a single 5mPoint
				}
				else{
					_logger.warning("PUTTING NaN at "+ nextTime);
					_output.produce(new VSTLF5MPoint(nextTime, Double.NaN, 0)); //into a single 5mPoint
				}
				
				// Move the time window to the next five minute
				prevTime = nextTime;
				nextTime = _cal.addSecondsTo(prevTime, _inc);
			}
		
		} // while (true)
	}

	private AggResult aggregate(Vector<Double> window, double filterThreshold) {
		// p exceeds the point of aggregation, aggregate the 4s points into a 5m points
		Vector<Double> filtered =
			microFilter(window, filterThreshold);  //filter the contents of the window into _filtered
		double ave = 0.0;
		if (filtered.size() > 0) {
			for(Double q : filtered) ave +=q; //average the contents of _filtered
			ave/=filtered.size();
			window.clear();//empty out the vectors for the next 5mBlock
		}
		else
			ave = Double.NaN;
		return new AggResult(ave, filtered.size());
	}
	
	private Vector<Double> microFilter(Vector<Double> window, double filterThreshold) {
		double[] array = new double[window.size()];
		for(int i = 0;i<window.size();i++) 
			array[i] = window.elementAt(i);
		
		try{
			Series load = new Series(array);
			Series smooth = (window.size() < 10 ? load : load.lowPassFR(10));
			array = new ThresholdChoiceFunction(filterThreshold).imageOf(load.minus(smooth),load,smooth).array();
		}catch(Exception e){
			_logger.warning("Error while micro filtering: " + e.getMessage());
			for(int i = 0;i<window.size();i++) array[i] = window.elementAt(i);
		}
		
		Vector<Double> filtered = new Vector<Double>();
		for(int i = 0;i<array.length;i++) {
			filtered.add(array[i]);	
		}
			
		return filtered;
	} 
	
	public void join() throws InterruptedException
	{
		_feedThread.join();
	}
}
