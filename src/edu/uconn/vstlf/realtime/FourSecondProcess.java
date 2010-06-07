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

public class FourSecondProcess extends Thread {
	private final PCBuffer<VSTLFObservationPoint> _input;	//Incoming observations appear here
	private final PCBuffer<VSTLF4SPoint> _4s;				//  They are aggregated into 4s points
															//		and placed in _4s.
	
	private Vector<VSTLF4SPoint> _window;			//Another thread moves each 4s point here
	
	private Vector<VSTLF4SPoint> _filtered;			//On the 5mTick, the _window is filtered to here
													// and then aggregated before it is
	private final PCBuffer<VSTLF5MPoint> _output;   //	moved to the out buffer
	
	private final PCBuffer<VSTLFMessage> _notif;	//Any messages for the user get placed here.
	
	private Date 				 _at;	//A record of the current time.  
										//  Nothing outside of the pulse should ever 
										// write to _at 
	private int _rate;			//The number of milliseconds between clock ticks.  This should ordinarily be 4000.
	
	private Date _lastAggTime;
	boolean _timeToAggregate;
	
	boolean _doFilter;
	
	int _maxLag;
	
	double _filterThreshold;
	
	public FourSecondProcess(PCBuffer<VSTLFMessage> notif, PCBuffer<VSTLFObservationPoint> buf,PCBuffer<VSTLF5MPoint> out) { 
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
		_at = st;
		// setup a scheduled action to compute the four second average and produce it (happens 
		// on four second boundaries.
		new Pulse(rate,new PulseAction() { // 250
			public boolean run(Date at) {
				Calendar cal = Items.makeCalendar();
				//_at = cal.lastTick(4, at);
				_at = cal.addSecondsTo(_at, 4);
				Vector<VSTLFObservationPoint> v;
				if(_rate < 4000){
					v = new Vector<VSTLFObservationPoint>();
					VSTLFObservationPoint thePoint = _input.consume();
					if (!thePoint.isValid())  {
						_4s.produce(new VSTLF4SPoint());
						return false;  // stop it all
					}
					v.add(thePoint);
				}
				else{
					v = _input.consumeAll();
					for(VSTLFObservationPoint pt : v)
						if (!pt.isValid()) {
							_4s.produce(new VSTLF4SPoint());
							return false;       // stop it all.
						}
				}
        		//System.out.format("AT: %s  [v.size: %d]\n", _at,v.size());
        		//Ignore any observations that are stamped more then <maxLag> seconds ago
        		//for(int i=0;i<v.size(); i++){
        			//System.err.format("\t4SBuffer[%d]:\t"+_at+":\tfound[%d] = %s\n",v.size(),i,v.elementAt(i).getStamp());
        		//}
    			Date boundary = cal.addSecondsTo(_at, -_maxLag);
        		//System.out.format("BOUNDARY: %s  [maxlag = %d]\n", _at,_maxLag);
        		
        		
        		for(int i=0;i<v.size(); i++){//System.err.println("DEBUG:\t"+_at+":\tfound "+v.elementAt(i).getStamp());
        			boolean canDiscard = v.elementAt(i).getStamp().before(boundary) || v.elementAt(i).getValue()<=0;
        			//System.err.format("\tcanDiscard?\t [%s] < [%s] => %b\n",v.elementAt(i).getStamp(),boundary,canDiscard);
        			if(canDiscard) 
        				v.remove(v.elementAt(i));        			
        		}
        		//System.err.format("number of entries left %d\n",v.size());
        		//Average whatever is left into a 4s Point
        		if(v.size()>0){
        			double ave = 0;
            		for(VSTLFObservationPoint p : v)
            			ave += p.getValue();
        			ave/=v.size();
        			//System.out.format("Got a 4s data point " + _at + " value: %f\n",ave);
        			_4s.produce(new VSTLF4SPoint(_at,ave));
        			_notif.produce(new FourSecMessage(_at,ave,v.size()));
        		}
        		else{//Complain if there are no observations since the last pulse.
        			_4s.produce(new VSTLF4SPoint(_at,0));
        			_notif.produce(new MessageMissing4s(_at));
        		}
        		return true;
			}
		},st);
	}
	
	/**
	 * Main loop of the 4sProcess.  Continually eats any fourS points produced by the
	 * 4s pulse.
	 */
	public void run() {
		boolean done = false;
		Calendar cal = Items.makeCalendar();
		_lastAggTime = cal.lastTick(300, _at);
		while(!done) {//A new point will come every four seconds
			VSTLF4SPoint thePoint = _4s.consume(); //take it out,
			if (!thePoint.isValid()) {
				_output.produce(new VSTLF5MPoint());
				break;
			}
			addAndMicroFilter(thePoint);	//add it to the vector to be aggregated
		}
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
			_notif.produce(new VSTLFExceptionMessage(e));
			for(int i = 0;i<window.size();i++) array[i] = window.elementAt(i).getValue();
		}
		for(int i = 0;i<array.length;i++) {
			_filtered.add(new VSTLF4SPoint(window.elementAt(i).getStamp(),array[i]));	
			if(window.elementAt(i).getValue() != array[i])
				_notif.produce(new VSTLF4sRefinementMessage(window.elementAt(i).getStamp(),window.elementAt(i).getValue(),array[i]));
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
}
