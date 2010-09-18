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

package edu.uconn.vstlf.gui;
import javax.swing.*;

import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.data.message.VSTLFMsgLogger;
import edu.uconn.vstlf.database.*;
import edu.uconn.vstlf.database.perst.*;
import edu.uconn.vstlf.database.xml.*;
import edu.uconn.vstlf.realtime.*;

/*
 * TWO HARD_CODED VALUES BELOW SHOULD BE SET DEPENDING ON THE PARTICULAR TEST YOU WOULD LIKE TO DO:
 * 
 * 	_TICK_INTERVAL is the number of milliseconds between pulses. This can be used to simulate in accelerated mode
 * 					HOWEVER, it must be set to 4000 for use with LIVE data in a production setting
 * 
 * 	_TEST_TIME is a Java Date object that represents the initial timestamp of the VSTLF Engine pulse. This can be
 * 					used to test on historical data present in data files, but for use with current, live data, should
 * 					be set to null.  Instructions to run tests from some specific dates appear below.
 * 
 */

public class IsoVstlfGui extends JFrame implements IVstlfMain, WindowListener,VSTLFNotificationCenter, PulseAction
{	
	// VSTLF related objects
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 5834770566430868929L;

	int _TICK_INTERVAL = 4000;
	
	//////////////////////////////////////////////////
	Calendar _cal;
	///////////////////////////////////////////////////
	//Set the Pulse/Startup time.
	//Specify a historical date like ...
	
	//Jun 2 2008						run with "java -Xmx512m true .rawLoad.pod .filtLoad.pod 4s_Jun2008-9.pod 5m_Jun2008-9.pod"
	//Date _TEST_TIME = _cal.newDate(2009, 2, 21, 3, 0, 0);
	
	//Live feed of current load;
	Date _TEST_TIME = null;
	////////////////////////////////////////////////////////
	
	Boolean _UPDATE_GUI = true;
	Boolean _DELETE = false;
	Date _prevTick;
	Date _pulseTime;
	
	VSTLFEngine _engine = null;
	String _dbName = ".vstlf";
	String _currentDataFileName = "current_data.xml";
	String _historyDataFileName = "24_hour_data.xml";
	private GetLoadData _loadData = new GetLoadData();
	boolean _coldStart;
	String _coldStartSrcType;
	String _currDataSrcType;
	PowerDB _db;	
	PowerDB _currDB;
	Pulse   _inputBeat;
	
	Series _aveP;
	Series _ave;
	Series _dev;
	Series _ovr;
	Series _und;
	Series _sumP;
	Series _sum;
	Series _sumOfSquares;
	int _nbErr;
	Date _end4SInput;

	// swing object variables
	JDesktopPane _desktop = null;
	boolean _aboutShown = false;
	JTabbedPane _mainFrame;
	JPanel _footer;
	JTextArea _aveLbl;
	LoadPlotFrame _plot;
	LogFrame _loggingFrame = null;
	Logger _logger = null;
	ForecastBrowserFrame _forecastFrame = null;
	HistoryFrame _fourSecondHistoryFrame = null;
	HistoryFrame _historyFrame = null;
	ForecastTableModel _forecastTableModel = null;
	HistoryTableModel _historyTableModel = null;
	DistributionFrame _distributionFrame = null;
	DistributionTableModel _distributionTableModel = null;
	// swing GUI update object
	ToolbarMgr _toolBarMgr;
	// static reference to this object
	boolean _debug = false;

	public IsoVstlfGui(boolean coldstart, String dbName, String currentDataXml, String dailyDataXml) throws Exception
   	{
		super("UCONN / ISO-NE VSTLF Test Set");
		_toolBarMgr = new ToolbarMgr(this);
		_cal = new Calendar(Items.get(Items.TimeZone));
		_coldStartSrcType = dailyDataXml.substring(dailyDataXml.lastIndexOf(".")+1).toLowerCase();
		_currDataSrcType = currentDataXml.substring(currentDataXml.lastIndexOf(".")+1).toLowerCase();
		this._coldStart = coldstart;
		_dbName = dbName;
		this._currentDataFileName = currentDataXml;
		this._historyDataFileName = dailyDataXml;
		this.addWindowListener(this);
   }
	
	public void setTestTime(Date tt){
		_TEST_TIME = tt;
	}
	
	public void setTestEndTime(Date tt){
		_end4SInput = tt;
	}
	
	public void setClockRate(int rate){
		_TICK_INTERVAL = rate;
	}
	
	public void init() throws Exception
	{
		logger  = new VSTLFMsgLogger("err.log", null);
		//Set up the GUI.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Indent the big window 25 pixels from each edge of the screen.
		int inset = 25;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(inset, inset,   screenSize.width - inset * 2,      screenSize.height - inset * 2);
		
		//create a tabbed interface with a log and a bunch of tabs (from left to right)
		createMainFrames();
		createLogFrame(); //Create logging window
		_logger.addMessage("Initializing...");
		_logger.addMessage("\t4s stream from '"+_currentDataFileName+"'");
		_logger.addMessage("\tInitial 12hrs of 5min data from '"+_historyDataFileName+"'");
		_logger.addMessage("\tStoring aggregated 5min data in '"+_dbName+"'");
		_logger.addMessage("");
		if(!_currDataSrcType.equals("xml")){
			_currDB = new PerstPowerDB(_currentDataFileName,4);
			_currDB.open();
			_logger.addMessage("source DB has:" + _currDB.getInfo());
			_end4SInput = _currDB.last("load");
			_logger.addMessage("Input ends @" + _end4SInput);
		}
		_logger.addMessage("Openning new working DB...");
		boolean good = setupDatabases();
		if (!good) return;
		_logger.addMessage("\t done.");
		
		//the tabs 
		createHistoryFrame();
		createForecastFrame();
		createPlotFrame();
		_toolBarMgr.createToolbars();
		setVisible(true);
		
		//set up book keeping for error distribution and read from file.
		importStatistics();
		
		// setup VSTLF engine and data feed
		if(_TEST_TIME == null){
			_TEST_TIME = _cal.now();
		}
		else{
			_logger.addMessage("TESTING FROM "+_TEST_TIME);
		}	
		this._engine = new VSTLFEngine(_db);
		this._engine.setUpdateRate(_TICK_INTERVAL);
		this._engine.setMacroFilteringOn(Items.isMacroFilterOn());
		this._engine.setMicroFilteringOn(Items.isMicroFilterOn());
		this._engine.setMacroFilterThreshold(Items.getMacroSpikeThreshold());
		this._engine.setMicroFilterThreshold(Items.getMicroSpikeThreshold());
		this._engine.setMaximumDataLag(Items.getMaximumDataLag());
		final Date ts = _cal.lastTick(4, _TEST_TIME);
		_pulseTime = ts;
		_inputBeat = new Pulse (_engine.getUpdateRate(), this, ts);  // This creates a thread that calls the run() method on a schedule (every ts msec). 	
		_engine.startCollecting(_cal.addSecondsTo(ts, 0));
		_prevTick= _cal.lastTick(300, _TEST_TIME);
		
		//load history
		Date last = _TEST_TIME;
		int tries = 0;
		do{
			try{
				LinkedList<LoadData> history = null;
				if(tries++>0){
					Thread.sleep(8192);
				}
				if (_coldStart) {
					if(_coldStartSrcType.equals("xml")){
						_logger.addMessage("COLD START:  Reading Load History from " + _historyDataFileName);
						this._loadData.getData(this._historyDataFileName, false);
						history = _loadData.getHistory();
						if(history.isEmpty())
							throw new Exception();
					}
					else{
						PerstPowerDB tempDB = new PerstPowerDB(_historyDataFileName,300);
						tempDB.open();
						Date tst = tempDB.first("load");
						_logger.addMessage("Init DB contains data in -\t("+tst+", "+tempDB.last("load")+"]");
						tst = _cal.addHoursTo(_prevTick, -12);
						_logger.addMessage("Extracting data in -\t("+tst+", "+_prevTick+"]");
						Series temp = tempDB.getLoad("load", tst, _prevTick);
						tempDB.close();
						history = new LinkedList<LoadData>();
						for(int i = 1;i<=temp.length();i++){
							history.addLast(new LoadData(temp.element(i),true,_cal.addMinutesTo(tst, 5*(i))));
						}
					}
					_db.fill("raw", history);
					_db.fill("filt", history);
					_logger.addMessage("Finished Write to Perst File.");
					last = history.getLast().getDate();
					_logger.addMessage("Recorded History:\t"+history.getFirst().getDate()+"\t<---------------->\t"+last);
					if(!last.equals(_prevTick))
						_logger.addMessage("Data not recent enough.  Trying to reload",true);
					
				}
			}catch(Exception e){
				_logger.addMessage("Couldn't read History file.  Will try again.");
			}
		}while(!last.equals(_prevTick));
		
		_engine.startProcessing();
		
		if(_UPDATE_GUI){
			Series theLoad = _db.getLoad("raw", _cal.addHoursTo(_db.last("raw"),-12), _db.last("raw"));
			theLoad = theLoad.reverse();
			final double[] value = new double[144];
			for(int ii = 1;ii<=144;ii++)
				value[ii-1] = theLoad.element(ii);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_historyTableModel.updateTableData(_cal.addHoursTo(_cal.lastTick(300,ts), 0), value);					
				}
			});
		}
		_logger.addMessage("Initialization Complete.\n\n\n\n\n");
	}
   
   boolean setupDatabases(){
	   try{
		    //Delete forecast database if it is around from last time;
		   File f = new File(_dbName);
			if(f.exists() && _DELETE) {
				if(!f.delete()) {
					MessageCenter.getInstance().put(
							new LogMessage(Level.SEVERE,
									"IsoVstlfGui", "setupDatabase", "???")); 
					System.exit(0);
				}
			}
			_db = new PerstPowerDB(_dbName,300);
			_db.open();
			return true;
	   }
	   catch(Exception e){
		   return false;
	   }		
   }
   
   VSTLFMsgLogger logger;

   void logErrors(Date at)
   {
	   try
	   {
		   logger.handle(new LogMessage(Level.INFO, "LogError", "LogError", "Error at " + at));

		   double [] mape = _aveP.array(false);
		   double [] mae = _ave.array(false);
		   double [] stdev = _dev.array(false);
		   double [] maxovr = _ovr.array(false);
		   double [] maxund = _und.array(false);
		   DataStream.logErr(logger, mape, "MAPE");
		   DataStream.logErr(logger, mae, "MAE");
		   DataStream.logErr(logger, stdev, "Std Dev");
		   DataStream.logErr(logger, maxovr, "Max Over Err");
		   DataStream.logErr(logger, maxund, "Max Under Err");
	   } catch (Exception e) {
	   		e.printStackTrace();
	   }
   }
   
   void updateErrors(Date at)
   {
	   try {
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
		} catch (Exception e) { e.printStackTrace(); }
   }
   
   void importStatistics(){
	   try{
		   _sum = new Series(12);
		   _sumP = new Series(12);
		   _sumOfSquares = new Series(12);
		   _ovr = new Series(12);
		   _und = new Series(12);
		   _nbErr = 0;
		   /*Date statDate = _db.last("stats");
		   Function maxi = new MaxFunction(),// mini = new MinFunction(),
	 				mae = new MAEFunction(), mape = new MAPEFunction(),
	 				sqr = new SquaringFunction(), sqrt = new SqrtFunction();
		   if(statDate==null){
			   Date st = _db.first("pred");
			   Date ed = _db.last("pred");
			   if(st == null || ed == null)
				   return;
			  for(Date t = st;!t.after(ed);t = _cal.addMinutesTo(t, 5)){
					Series a = _db.getLoad("filt", t, _cal.addHoursTo(t, 1));
					Series p = _db.getForecast(t);
					if(p!=null && a.countOf(Double.NaN)==0){
						Series err = mae.imageOf(a,p);
						Series dif = p.minus(a);
						Series minDif = a.minus(p);
						Series errP = mape.imageOf(a,p);
						_sum = _sum.plus(err);
						_sumP = _sumP.plus(errP);
						_sumOfSquares = _sumOfSquares.plus(sqr.imageOf(err));
						_ovr = maxi.imageOf(_ovr,dif);
						_und = maxi.imageOf(_und,minDif);
						_nbErr++;
					}
			   }
		   }
		   else{
			   MessageCenter.getInstance().put(
					   new LogMessage(Level.INFO, "IsoVstlfGui", "importStatistics", "Reading from TABLE"));
			   _sum = _db.getSum();
			   _sumP = _db.getSumP();
			   _sumOfSquares = _db.getSumOfSquares();
			   _ovr = _db.getMaxOvr();
			   _und = _db.getMaxUnd();
			   _nbErr = _db.getNb();
		   }
		   if(_nbErr > 0){
			   Function div = new DivisorFunction(_nbErr);
			   Series aveP = div.imageOf(_sumP);
			   Series ave = div.imageOf(_sum);
			   Series dev = sqrt.imageOf(div.imageOf(_sumOfSquares).minus(sqr.imageOf(ave)));
			   _distributionTableModel.setMean(aveP.array(false), ave.array(false));
			   _distributionTableModel.setDev(dev.array(false));
			   _distributionTableModel.setMaxOvr(_ovr.array(false));
			   _distributionTableModel.setMaxUndr(_und.array(false));
		   }*/
	   }
	   catch(Exception e){
		   MessageCenter.getInstance().put(
				   new LogMessage(Level.SEVERE, "IsoVstlfGui", "importStatistics", "Exception while populating statistics from history"));
		   e.printStackTrace();
	   }
   }

   protected void createMainFrames(){
	   _mainFrame = new JTabbedPane();
	   this.getContentPane().add(BorderLayout.CENTER,_mainFrame);
	   _mainFrame.setVisible(true);
	   _footer = new JPanel();
	   this.getContentPane().add(BorderLayout.PAGE_END,_footer);
	   _footer.setVisible(true);
	   _footer.setLayout(new BorderLayout());
	   _footer.setPreferredSize(new Dimension(200,256));
 
   }
   protected void createLogFrame()
   {
      _loggingFrame = new LogFrame();
      _loggingFrame.setVisible(true);
      _logger = new Logger(_loggingFrame);
      _footer.add(BorderLayout.CENTER,_loggingFrame);
      //_aveLbl = new JTextArea();
      //_aveLbl.setVisible(true);
      _distributionTableModel = new DistributionTableModel();
      _distributionFrame = new DistributionFrame(_distributionTableModel,"Data Distribution");
      _distributionFrame.setVisible(true);
      //_distributionFrame.setMaximumSize(new Dimension(200,200));
      _footer.add(BorderLayout.LINE_END,new JScrollPane(_distributionFrame));
      //_desktop.add(_loggingFrame);
   }
   
   protected void createPlotFrame() {
	   _plot = new LoadPlotFrame();
	   _plot.setVisible(true);
	   _mainFrame.addTab("Signal Plot", _plot);
   }

   protected void createForecastFrame()
   {
	   _forecastTableModel = new ForecastTableModel();
      _forecastFrame = new ForecastBrowserFrame(_forecastTableModel, "One Hour Prediction",_db);
      _forecastFrame.setVisible(true);
      _mainFrame.addTab("Forecast Browser",_forecastFrame);
   }
   protected void createHistoryFrame()
   {
	  _historyTableModel = new HistoryTableModel();
      _historyFrame = new HistoryFrame(_historyTableModel, "Actual 5-Minute History");
      _historyFrame.setVisible(true);
      _mainFrame.addTab("History Table", _historyFrame);
   }

   /**
    * This is the entry-point for the pulsating thread. 
    * The run method first update the pulse time and proceeds with the retrieval 
    * of the next 4 second data point. It then invokes the addObserveration method of
    * the engine to record the observation. This is the routine that must be modified to
    * hook up to a different data source.  
    */
	public boolean run(Date at) {
		_pulseTime = _cal.addSecondsTo(_pulseTime, 4);
		at = _pulseTime;
		LoadData currentData;
		try {	
			if (at.after(_end4SInput)) {
				_engine.stop();
				_engine.join();
					
		        MessageCenter.getInstance().put(new VSTLFMessage(VSTLFMessage.Type.StopMessageCenter));
				MessageCenter.getInstance().join();
				logErrors(at);
		        return false;
			}
			if(_currDataSrcType.equals("xml")){
				this._loadData.getData(this._currentDataFileName, false);
				currentData = this._loadData.getCurrentData();
			} else{
				currentData = new LoadData(_currDB.getLoad("load", at),true,at);
			}
			//_logger.addMessage("currentData at [" + at.toString() + "] is " + (currentData==null ? "null" : currentData.toString()));
			_engine.addObservation(VSTLFMessage.Type.RT4sPoint, currentData.getDate(), currentData.getValue());
			// update the toolbars
			if(_UPDATE_GUI){
				_toolBarMgr.update(at, currentData.getDate(), currentData.getValue());
			}
		}
		catch (org.xml.sax.SAXParseException e){
			_logger.addMessage("Error Parsing XML");
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			_logger.addMessage("Could not collect observation");
			return true;
		}
		return true;
	}


// VSTLF Notification Implementation

	public void fourSTick(Date at,double val, int nbObs){
		if(_TICK_INTERVAL==4000)
			_logger.addMessage(nbObs+" observations aggregated to "+val+" MW @ "+at);
	}

	public void fiveMTick(final Date at, final double val,final int nbObs) {
		updateErrors(at);
		
		if(_UPDATE_GUI){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_logger.addMessage("Aggregate 5m point = "+val+" @ "+at+" from "+nbObs+" 4s points.");
					//Update history table
					try{
						Date twelveHrsAgo = _cal.addHoursTo(at, -12);
						Series history = _db.getLoad("raw", twelveHrsAgo, at);
						double[] value = history.reverse().array(false);
						_historyTableModel.updateTableData(at, value);					
					
						Date ovr1HrAgo = _cal.addMinutesTo(at, -65);
						Date fvMinsAgo = _cal.addMinutesTo(at, -5);
						Series pred = _db.getForecast(ovr1HrAgo);
						Series act = _db.getLoad("filt", ovr1HrAgo, fvMinsAgo);
						if(pred!=null && act.countOf(Double.NaN)==0 &&!_db.last("filt").before(at)){
							//Update error distribution table
							_distributionTableModel.setMean(_aveP.array(false), _ave.array(false));
							_distributionTableModel.setDev(_dev.array(false));
							_distributionTableModel.setMaxOvr(_ovr.array(false));
							_distributionTableModel.setMaxUndr(_und.array(false));
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			//maybe dump log
			if(_cal.getHour(at)==0 && _cal.getMinute(at)==0){
				Date logDay = _cal.addDaysTo(at, -1);
				_loggingFrame.dump(".log."+_cal.getYear(logDay)
										  +"."+(_cal.getMonth(logDay)+1)
										  +"."+_cal.getDate(logDay)+".vstlf");
			}
		}
	}

	public void didPrediction(final Date at, final double[] val) {
		try {
			//this._logger.addMessage("Forecast from "+at);
			// update the JTable underlying data model ForecastTableModel
			try{
				_db.addForecastArray(at, val);
			} catch(Exception e){
				e.printStackTrace();
			}
			if(_UPDATE_GUI) {
				final double actual = _db.getLoad("filt", at);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						_forecastFrame.addDate(at);						
						_plot.addPoint(at, val,actual);
					}
				});
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void missing4SDataPoint(final Date at) {
		if(_UPDATE_GUI) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_logger.addMessage("No observation recorded between " + _cal.addSecondsTo(at, -4)+" and "+at);					
				}
			});
		}
	}

	public void missing5MDataPoint(final Date at) {
		if(_UPDATE_GUI) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_logger.addMessage("Missing 5 minutedata point @ " + at);
					_historyTableModel.updateTableMissingData(at);					
				}
			});
		}
	}

	public void refined4SPoint(final Date at,final double oldVal,final double newVal) {
		if(_UPDATE_GUI) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_logger.addMessage("refined (micro filtering) 4s data point @ " + at + " from " + oldVal + " to " + newVal);
				}
			});
		}
	}

	public void refined5MPoint(final Date at,final double oldVal,final double newVal) {
		if(_UPDATE_GUI) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_logger.addMessage("refined (macro filtering) 5m data point @ " + at + " from " + oldVal + " to " + newVal);
					_historyTableModel.updateTableFilteredData(at, oldVal, newVal);					
				}
			});
		}
	}
	public void beginTraining(Date at, Date from, Date to)
	{
	}
	public void endTraining(Date at, Date til)
	{

	}
	public void exceptionAlert(Exception e){
		if(_UPDATE_GUI)
			_logger.addMessage("RUNTIME EXCEPTION:  \""+e.getMessage()+"\"");
		e.printStackTrace();
	}
// end VSTLF Notification callbacks


   public String getStackTrace(Exception e)
   {
      StringWriter s = new StringWriter();
      e.printStackTrace(new PrintWriter(s));
      return s.toString();
   }

   class FilteredStream extends FilterOutputStream {
      public FilteredStream(OutputStream aStream)
      {
         super(aStream);
      }

      public void write(byte b[]) throws IOException
      {
         String aString = new String(b);
         _logger.addMessage(aString);
      }

      public void write(byte b[], int off, int len) throws IOException
      {
         String aString = new String(b, off, len);
         _logger.addMessage(aString);
      }
   }
   
   
   //Required by WindowListener Interface
   public void windowOpened(WindowEvent e){}
   public void windowClosing(WindowEvent e){
	   windowClosed(e);
   }
   public void windowClosed(WindowEvent e){
	   //System.out.println("Closing...");
	   _db.addStats(_pulseTime, _nbErr, _sum.array(), _sumP.array(), _sumOfSquares.array(), _ovr.array(), _und.array());
	   try{
		   _db.close();
		   if(!_currDataSrcType.equals("xml"))
			   _currDB.close();
	   }
	   catch(Exception x){
		   x.printStackTrace();
	   }
   }
   public void windowActivated(WindowEvent e){}
   public void windowDeactivated(WindowEvent e){}
   public void windowIconified(WindowEvent e){}
   public void windowDeiconified(WindowEvent e){}

   public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("java edu.uconn.gui.IsoVstlfGui <coldstart> <rawLoad db> <filteredLoad db> <currentDataFile> <24hrDataFile>");
			return;
		}
      try {
    	  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         IsoVstlfGui frame = new IsoVstlfGui(Boolean.valueOf(args[0]), args[1], args[2], args[3]);
         frame.init();
      } catch (Exception e) {
         System.out.println(e.toString());
         e.printStackTrace();
         return;
      }
   }
}
