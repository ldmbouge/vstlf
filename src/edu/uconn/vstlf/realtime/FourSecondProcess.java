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
import java.util.Vector;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;

public class FourSecondProcess extends Thread {
	private final PCBuffer<VSTLFObservationPoint> _input;	//Incoming observations appear here
	private final PCBuffer<VSTLF4SPoint> _4s;				//  They are aggregated into 4s points
															//		and placed in _4s.
	
	private Vector<VSTLF4SPoint> _window;			//Another thread moves each 4s point here
	
	private Vector<VSTLF4SPoint> _filtered;			//On the 5mTick, the _window is filtered to here
													// and then aggregated before it is
	private final PCBuffer<VSTLF5MPoint> _output;   //	moved to the out buffer
	
	private final MessageCenter _notif;	//Any messages for the user get placed here.
	
	private Date 				 _at;	//A record of the current time.  
										//  Nothing outside of the pulse should ever 
										// write to _at 
	private int _rate;			//The number of milliseconds between clock ticks.  This should ordinarily be 4000.
	
	private Date _lastAggTime;
	boolean _timeToAggregate;
	
	boolean _doFilter;
	
	int _maxLag;
	
	double _filterThreshold;
	
	Integrate4sLoad _itgThread;
	
	public FourSecondProcess(MessageCenter notif, PCBuffer<VSTLFObservationPoint> buf,PCBuffer<VSTLF5MPoint> out) { 
		_input = buf;
		_output = out;
		_notif = notif;
		_4s = new PCBuffer<VSTLF4SPoint>();
		_window = new Vector<VSTLF4SPoint>(75);
		_filtered = new Vector<VSTLF4SPoint>(75);
		_doFilter = true;
		_maxLag = 8;
		_timeToAggregate = false;
		_filterThreshold = 50;
	}
	
	
	public void prepare(Date st,int rate){
		_rate = rate;
		synchronized(this) {
			_at = st;
		}
		// setup an action to compute the four second average and produce it (happens 
		// on four second boundaries.
		_itgThread = new Integrate4sLoad(_input, _4s, _at, 4);
		_itgThread.init();
	}
	
	/**
	 * Main loop of the 4sProcess.  Continually eats any fourS points produced by the
	 * 4s pulse.
	 */
	public void run() {
		//boolean done = false;
		Calendar cal = Items.makeCalendar();
		synchronized(this) { 
			_lastAggTime = cal.lastTick(300, _at);		
		}
		VSTLF4SPoint thePoint;
		while( (thePoint = _4s.consume()).getType() != VSTLFMessage.Type.EOF ) {//A new point will come every four seconds			
			if (!thePoint.isValid()) {
				_output.produce(new VSTLF5MPoint());
				break;
			}
			addAndMicroFilter(thePoint);	//add it to the vector to be aggregated
		}
		_output.produce(new VSTLF5MPoint(VSTLFMessage.Type.EOF, _at, 0.0, 0));
	}
	/**
	 * Adds a point p the the running window of 4s data.
	 * Run the filter over the next window shift.
	 * Leave all the observation in the 5 minute window.
	 * Another background thread will check on the 5 minute mark
	 * To pickup the window content and aggregate. 
	 * @param p
	 */
	synchronized private void addAndMicroFilter(VSTLF4SPoint p) {
		if(p._val>0)
			_window.add(p);					//put the new point in the window (maybe)
		Calendar cal = Items.makeCalendar();
		Date priorBoundary = cal.lastTick(300,p._at);
		//System.err.println(_at+" ------ got : "+p);
		boolean aggNow = priorBoundary.after(this._lastAggTime);
		//System.err.format("=======> LAST %s BOUNDARY: %s  now? %b\n",_lastAggTime,priorBoundary,aggNow);
		if(aggNow){ //if five minutes have elapsed
			_lastAggTime = priorBoundary;
			microFilter(_window);  //filter the contents of the window into _filtered
			if (_filtered.size() > 0) {
				double ave = 0.0;
				for(VSTLF4SPoint q : _filtered) ave +=q.getValue(); //average the contents of _filtered
				ave/=_filtered.size();
				_output.produce(new VSTLF5MPoint(priorBoundary,ave,_filtered.size())); //into a single 5mPoint
				//_notif.produce(new FiveMinMessage(priorBoundary,ave,_filtered.size()));
				_filtered.clear();		//empty out the vectors for the next 5mBlock
				_window.clear();
			}
			else{//System.err.println("PUTTING NaN at "+ priorBoundary);
				_output.produce(new VSTLF5MPoint(priorBoundary,Double.NaN,0)); //into a single 5mPoint
			}
		}
	}
	
	/**
	 * Performs the micro spike filtering.
	 * @param window
	 */
	private void microFilter(Vector<VSTLF4SPoint> window) {
		if(!_doFilter){
			_filtered = window;
			return;
		}
		double[] array = new double[window.size()];
		for(int i = 0;i<window.size();i++) array[i] = window.elementAt(i).getValue();
		try{
			Series load = new Series(array);
			Series smooth = (window.size() < 10 ? load : load.lowPassFR(10));
			array = new ThresholdChoiceFunction(_filterThreshold).imageOf(load.minus(smooth),load,smooth).array();
		}catch(Exception e){
			_notif.put(new VSTLFRealTimeExceptionMessage(e));
			for(int i = 0;i<window.size();i++) array[i] = window.elementAt(i).getValue();
		}
		for(int i = 0;i<array.length;i++) {
			_filtered.add(new VSTLF4SPoint(window.elementAt(i).getStamp(),array[i]));	
			if(window.elementAt(i).getValue() != array[i])
				_notif.put(new VSTLF4sRefinementMessage(window.elementAt(i).getStamp(),window.elementAt(i).getValue(),array[i]));
		}
			
	}
	
	public void useFilter(boolean on){
		_doFilter = on;
	}
	
	public void setFilterThreshold(double t){
		_filterThreshold = t;
	}
	
	public void setMaxLag(int lag){
		_maxLag = lag;
	}
	
	public void aggjoin() throws InterruptedException
	{
		_itgThread.join();
	}
}
