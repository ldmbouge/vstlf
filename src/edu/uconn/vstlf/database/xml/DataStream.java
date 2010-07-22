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

package edu.uconn.vstlf.database.xml;
import java.io.*;
import java.util.Date;
import java.text.*;
import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.realtime.*;
import edu.uconn.vstlf.config.*;

public class DataStream implements edu.uconn.vstlf.realtime.VSTLFNotificationCenter{
	DateFormat _df;
	StringWriter _bufWriter = null;
	BufferedReader _in = null;
	PrintWriter _out = null;
	StringBuffer _buf = null;
	IsoVstlf _srvr = null;
	Calendar _cal;
	boolean _isInput,_isOutput, _waiting = false;
	
	public DataStream(){
		_cal = Items.makeCalendar();
		_bufWriter = new StringWriter();
		_buf = _bufWriter.getBuffer();
		_df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	}

	public DataStream(InputStream in){
		this();
		if(in!=null)
			_in = new BufferedReader(new InputStreamReader(in));
		else
			_in = null;
		_isInput = true; _isOutput = false;
	}
	
	public DataStream(OutputStream out){
		this();
		if(out!=null)
			_out = new PrintWriter(out);
		else
			_out = null;
		_isInput = false; _isOutput = true;
	}
	
	public DataStream(IsoVstlf src, OutputStream dst){
		this(dst);
		_srvr = src;
	}
	
	public DataStream(InputStream src, IsoVstlf dst){
		this(src);
		_srvr = dst;
	}
	
	public synchronized void setPipe(InputStream in) throws Exception {
		if(_isInput)
			_in = new BufferedReader(new InputStreamReader(in));
		else
			throw new Exception("Not an Input Stream");
	}
	
	public synchronized void setPipe(OutputStream out) throws Exception {
		if(_isOutput){
			_out = new PrintWriter(out);
			_out.print(_buf);
			_waiting = false;
		}
		else
			throw new Exception("Not an Output Stream");
	}
	
	
	public synchronized VSTLF4SPoint get()throws Exception{
		while(_buf.indexOf("</data>") < 0){
			_buf.append(_in.readLine());
		}
		int stTime, edTime, stVal, edVal;
		stTime = _buf.indexOf("<time>")+6;
		edTime = _buf.indexOf("</time>")-1;
		stVal =  _buf.indexOf("<value>")+7;
		edVal =  _buf.indexOf("</value>")-1;		
		String tString = _buf.substring(stTime,edTime).trim();
		double v = Double.parseDouble(_buf.substring(stVal, edVal).trim());
		_buf.delete(0,_buf.length()-1);
		Date t = _df.parse(tString);
		return new VSTLF4SPoint(t,v);
	}
	
	void send(String xml){
		if(_out==null || _out.checkError()){		//if there is no outpipe
			_out = new PrintWriter(_bufWriter);		//	then accumulate xml in the buffer
			if(!_waiting){
				_waiting = true;
				_srvr.getNewClient();
			}
		}
		_out.println(xml);
		_out.flush();
		
		if(_buf.length() > 1024*1024){
			dump();
		}
	}
	
	void sendOpen(String name){
		send("<"+name+">");
	}
	
	void sendClose(String name){
		send("</"+name+">");
	}
	
	void sendContent(String content){
		send(content);
	}
	
	//vstlfNotificationCenter methods
	public void fourSTick(Date at, double val, int nbObs){
		sendOpen("4sObservation");
			sendOpen("time");
				sendContent(_df.format(at));
			sendClose("time");
			sendOpen("value");
				sendContent(Double.toString(val));
			sendClose("value");
			sendOpen("number-of-observations");
				sendContent(Integer.toString(nbObs));
			sendClose("number-of-observations");
		sendClose("4sObservation");
	}
	
	public void fiveMTick(final Date at, final double val,final int nbObs) {
		sendOpen("5mObservation");
			sendOpen("time");
				sendContent(_df.format(at));
			sendClose("time");
			sendOpen("value");
				sendContent(Double.toString(val));
			sendClose("value");
			sendOpen("number-of-observations");
				sendContent(Integer.toString(nbObs));
			sendClose("number-of-observations");
		sendClose("5mObservation");
	}

	public void didPrediction(final Date at, final double[] val) {
		sendOpen("forecast");
			sendOpen("time");
				sendContent(at.toString());
			sendClose("time");
			for(int i = 0;i<12;i++){
				sendOpen("data");
					sendOpen("value");
						sendContent(Double.toString(val[i]));
					sendClose("value");
					sendOpen("time");
						sendContent(_df.format(_cal.addMinutesTo(at, 5*(i+1))));
					sendClose("time");
				sendClose("data");
			}
		sendClose("forecast");
	}

	public void missing4SDataPoint(final Date at) {
		sendOpen("missing-4s-Observation");
			sendContent(_df.format(at));
		sendClose("missing-4s-Observation");
		
	}

	public void missing5MDataPoint(final Date at) {
		sendOpen("missing-5m-Observation");
			sendContent(_df.format(at));
		sendClose("missing-5m-Observation");

	}

	public void refined4SPoint(final Date at,final double oldVal,final double newVal) {
		sendOpen("micro-filter");
			sendOpen("time");
				sendContent(_df.format(at));
			sendClose("time");
			sendOpen("from");
				sendContent(Double.toString(oldVal));
			sendClose("from");
			sendOpen("to");
			sendContent(Double.toString(newVal));			
			sendClose("to");
		sendClose("micro-filter");
	}

	public void refined5MPoint(final Date at,final double oldVal,final double newVal) {
		sendOpen("macro-filter");
			sendOpen("time");
				sendContent(_df.format(at));
			sendClose("time");
			sendOpen("from");
				sendContent(Double.toString(oldVal));
			sendClose("from");
			sendOpen("to");
				sendContent(Double.toString(newVal));			
			sendClose("to");
		sendClose("macro-filter");
	}
	public void beginTraining(Date at, Date from, Date to){}
	public void endTraining(Date at, Date til){}
	public void exceptionAlert(Exception e){
		sendOpen("run-time-exception");
			sendContent(getStackTrace(e));
		sendClose("run-time-exception");
	}	
	String getStackTrace(Exception e){
		StringWriter s = new StringWriter();
		e.printStackTrace(new PrintWriter(s));
		return s.toString();
	}
	
	synchronized public void dump(){
		Date d = new Date();
		FileWriter fw = null;
    	try{
    		fw = new FileWriter(new File("unsentXML-"+d));
    		fw.write(_buf.toString());
    		fw.close();
    	}
    	catch(Exception e){
    		return;
    	}
    	_buf.delete(0, _buf.length()-1);  	
    }
}
