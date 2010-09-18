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
import java.util.logging.Level;
import java.text.*;

import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.data.doubleprecision.DivisorFunction;
import edu.uconn.vstlf.data.doubleprecision.Function;
import edu.uconn.vstlf.data.doubleprecision.MAEFunction;
import edu.uconn.vstlf.data.doubleprecision.MAPEFunction;
import edu.uconn.vstlf.data.doubleprecision.MaxFunction;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.doubleprecision.SqrtFunction;
import edu.uconn.vstlf.data.doubleprecision.SquaringFunction;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMsgLogger;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.realtime.*;
import edu.uconn.vstlf.config.*;

public class DataStream implements edu.uconn.vstlf.realtime.VSTLFNotificationCenter{
	private DateFormat _df;
	private StringWriter _bufWriter = null;
	private BufferedReader _in = null;
	private PrintWriter _out = null;
	private StringBuffer _buf = null;
	private IsoVstlf _srvr = null;
	private Calendar _cal;
	boolean _isInput,_isOutput, _waiting = false;
	
	private boolean _test = false;
	
	public void SetTest(boolean tst) { _test = tst; }
	
	Series _aveP;
	Series _ave;
	Series _dev;
	Series _ovr;
	Series _und;
	Series _sumP;
	Series _sum;
	Series _sumOfSquares;
	int _nbErr;
	
	PowerDB _db;
	public void setDB(PowerDB db) { _db = db; }
	
	public DataStream(){
		_cal = Items.makeCalendar();
		_bufWriter = new StringWriter();
		_buf = _bufWriter.getBuffer();
		_df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		
		try {
		   _sum = new Series(12);
		   _sumP = new Series(12);
		   _sumOfSquares = new Series(12);
		   _ovr = new Series(12);
		   _und = new Series(12);
		   _nbErr = 0;
		   logger = new VSTLFMsgLogger("err.log", null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
	
	private void send(String xml){
		if (_test) return;
		
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
	
	private void sendOpen(String name){
		send("<"+name+">");
	}
	
	private void sendClose(String name){
		send("</"+name+">");
	}
	
	private void sendContent(String content){
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
		
		//Update error distribution table
		
		try {
			if (at.getTime() % (1000*60*60) == 0)
				MessageCenter.getInstance().put(new LogMessage(Level.INFO, "DataStream", "fiveMTick", "Aggregate 5m points for "+ at));
			Date ovr1HrAgo = _cal.addMinutesTo(at, -65);
			Date fvMinsAgo = _cal.addMinutesTo(at, -5);
			Series pred = _db.getForecast(ovr1HrAgo);
			Series act = _db.getLoad("filt", ovr1HrAgo, fvMinsAgo);
			if(pred!=null && act.countOf(Double.NaN)==0 &&!_db.last("filt").before(at)){
				Series error = new MAEFunction().imageOf(act,pred);
				Series errorP = new MAPEFunction().imageOf(act,pred);
				_nbErr++;
				_sum = _sum.plus(error);
				_sumP = _sumP.plus(errorP);
				Function n = new DivisorFunction(_nbErr);
				Function s = new SquaringFunction();
				Function max = new MaxFunction();
				Series ovr = pred.minus(act);
				Series und = act.minus(pred);
				_ovr = max.imageOf(_ovr, ovr);
				_und = max.imageOf(_und, und);
				_sumOfSquares = _sumOfSquares.plus(s.imageOf(error));
				_ave = n.imageOf(_sum);
				_aveP = n.imageOf(_sumP);
				_dev = new SqrtFunction().imageOf(  n.imageOf(_sumOfSquares).minus(s.imageOf(_ave)));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
		
			try{
				_db.addForecastArray(at, val);
			} catch(Exception e){
				e.printStackTrace();
			}

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
	private String getStackTrace(Exception e){
		StringWriter s = new StringWriter();
		e.printStackTrace(new PrintWriter(s));
		return s.toString();
	}
	
	synchronized private void dump(){
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
	
	VSTLFMsgLogger logger;
	public void logErrors(Date at)
	{
		try
		{
			logger.handle(new LogMessage(Level.INFO, "LogError", "LogError", "Error at" + at));

		double [] mape = _aveP.array(false);
		double [] mae = _ave.array(false);
		double [] stdev = _dev.array(false);
		double [] maxovr = _ovr.array(false);
		double [] maxund = _und.array(false);
		logErr(logger, mape, "MAPE");
		logErr(logger, mae, "MAE");
		logErr(logger, stdev, "Std Dev");
		logErr(logger, maxovr, "Max Over Err");
		logErr(logger, maxund, "Max Under Err");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	synchronized public void printSummary()
	{

			double [] mape = _aveP.array(false);
			double [] mae = _ave.array(false);
			double [] stdev = _dev.array(false);
			double [] maxovr = _ovr.array(false);
			double [] maxund = _und.array(false);
			_out.println("Minutes Ahead\tMAPE\t\t\t\tMAE\t\t\t\tStDev\t\t\t\tMax Ovr Err\t\t\tMax Und Err");
			for (int i = 0; i < 12; ++i) 
				_out.println( (i+1)*5 + "\t\t" + mape[i] + "\t\t" + mae[i] + "\t\t" + stdev[i] + "\t\t" + maxovr[i] + "\t\t" + maxund[i]);
			_out.flush();
	}
	
	static public void logErr(VSTLFMsgLogger logger, double[] err, String type) throws Exception
	{			
		String msg = type + ":\t";
		for (int i = 0; i < err.length; ++i)
			msg += err[i] + "\t";
		logger.handle(new LogMessage(Level.INFO, "LogError", "LogError", msg));
	}
}
