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

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.database.*;
import edu.uconn.vstlf.neuro.ANNFactory;

public class VSTLFEngine {
	private final PCBuffer<VSTLFObservationPoint> _obs;
	private final PCBuffer<VSTLF5MPoint> _fiveMinute;
	private final MessageCenter _notif;
	private Calendar _cal;
	private PowerDB _db;
	private int     _milliseconds; // rate of updates
	FourSecondProcess _fsp;
	FiveMinuteProcess _fmp;
	private ANNFactory _annfac;
	
	public VSTLFEngine(PowerDB db, ANNFactory annfac) {
		_annfac = annfac;
		_obs = new PCBuffer<VSTLFObservationPoint>();
		_fiveMinute = new PCBuffer<VSTLF5MPoint>();
		_notif      = MessageCenter.getInstance();//new PCBuffer<VSTLFRealTimeMessage>(1024);
		_cal        = Items.makeCalendar();
		_db	= db;//"RawFiveMinuteLoad.pod";
		_fsp = new FourSecondProcess(_notif,_obs,_fiveMinute);
		_fmp = new FiveMinuteProcess(_notif,_fiveMinute,_db, _annfac);
		_milliseconds = 1000;
	}
	
	public void startup(Date at) {
		System.out.println("Here");
		_fsp.prepare(at,_milliseconds);
		_fsp.start();
		_fmp.start();
	}
	public void stop() {
		_obs.produce(new VSTLFObservationPoint(VSTLFMessage.Type.EOF));
	}
	public void startup() {
		startup(new Date(System.currentTimeMillis()));
	}
	
	public void startCollecting(Date at){
		_fsp.prepare(at,_milliseconds);
		_fsp.start();
	}
	
	public void startProcessing(){
		_fmp.start();
	}
	
	public void addObservation(VSTLFMessage.Type type, Date at,double val) {
		_cal.setTime(at);
		assert(_cal.get(Calendar.MILLISECOND) == 0);
        _obs.produce(new VSTLFObservationPoint(type, at,val));
        /*while (!_notif.empty()) {
        	_notif.consume().visit(center);
        }*/
	}
	
	public void revise5MPoint(Date at,double val) {
		_db.addLoad("raw", at, val);
		_db.addLoad("filt", at, val);
		_db.commit();
		
	}
	
	public void setMicroFilteringOn(boolean on){
		_fsp.useFilter(on);
	}
	
	public void setMacroFilteringOn(boolean on){
		_fmp.useFilter(on);
	}
	
	public void setMicroFilterThreshold(double t){
		_fsp.setFilterThreshold(t);
	}
	
	public void setMacroFilterThreshold(double t){
		_fmp.setFilterThreshold(t);
	}
	
	public void setMaximumDataLag(int lag){
		_fsp.setMaxLag(lag);
	}
	
	public void setUpdateRate(int rate) {
		_milliseconds = rate;
	}
	public int getUpdateRate() {
		return _milliseconds;
	}
	
	public void join() throws InterruptedException
	{
		_fsp.aggjoin();
		_fsp.join();
		_fmp.join();
	}
}
