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

package edu.uconn.vstlf.database.perst;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collection;
import java.util.logging.Level;

import org.garret.perst.*;
import org.garret.perst.TimeSeries.Tick;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.xml.*;
public class PerstPowerDB extends PowerDB {

	Storage _db;
	String _table;
	FieldIndex<PerstDataSeries> _fields;
	int _inc;

	public PerstPowerDB(String table, int inc){
		_db = StorageFactory.getInstance().createStorage();
		_db.setProperty("perst.multiclient.support", Boolean.TRUE);
		_table = table;
		_inc = inc;
	}

	public static PerstPowerDB fromXML(String outFile, int inc, String inFile)throws Exception{
		String methodName = PerstPowerDB.class.getMethod("fromXML", 
				new Class[]{String.class, Integer.TYPE, String.class}).getName();
		Calendar cal = Items.makeCalendar();
		//DateFormat dateFormat =  cal.getDateFormat("M/dd/yyyy h:mm:ss a");
		
		//System.err.println("Parsing XML");
		ParseTrainingData d = new ParseTrainingData(inFile);
		d.parseData();
		ParseParameters p = d.getParameters();
		//Date startDate = cal.lastTick(inc,dateFormat.parse(p.getStartTime()));
		//Date endDate = cal.lastTick(inc, dateFormat.parse(p.getEndTime()));
		//System.out.format("Importing %s to %s\n", startDate,endDate);
		int xInc = Integer.parseInt(p.getResolution());
		//System.err.println(xInc);
		if (inc < xInc || inc%xInc > 0)
			throw new Exception("The input resolution does not divide the requested resolution");
		LinkedList<LoadData> loadData = d.getHistory();
		Date prev = null;
		for(LoadData n : loadData) {
			//System.out.format("at %d got: %s\n",j++,n.getDate());
			n.setDate(cal.lastTick(inc,n.getDate()));// align the date			
			if (prev != null && !prev.before(n.getDate()))
				MessageCenter.getInstance().put(
						new LogMessage(Level.WARNING, PerstPowerDB.class.getName(),
								methodName, String.format("\ntimestamp were not in increasing order! last=%s  now=%s\n", prev,n.getDate())));
			prev = n.getDate();
		}
		//System.err.println("Sorting List");
		//Collections.sort(loadData);  // Can't sort! This would destroy the ordering for imports with DST transition days!
		LoadData[] array = new LoadData[loadData.size()];
		int secInDay = 24 * 60 * 60;
		if  (secInDay/inc != loadData.size())
			MessageCenter.getInstance().put(
					new LogMessage(Level.INFO, PerstPowerDB.class.getName(),
							methodName, String.format("\nExpecting a vector of %d entries. Got a vector of %d\n",secInDay/inc,loadData.size())));
		loadData.toArray(array);
		for(int k=1;k<array.length;k++) {
			LoadData n = array[k];
			assert(k>=1);
			Date now = cal.lastTick(inc, cal.addSecondsTo(array[k-1].getDate(), inc));
			if (!now.equals(n.getDate())) {
				MessageCenter.getInstance().put(
						new LogMessage(Level.WARNING, PerstPowerDB.class.getName(),
								methodName, String.format("We have a gap: expecting: %s got: %s\n",now, n.getDate())));
			}
			if (n.getValue() == 0.0) {
				MessageCenter.getInstance().put(
						new LogMessage(Level.WARNING, PerstPowerDB.class.getName(),
								methodName, String.format("Load of %f at %s\n",n.getValue(),n.getDate())));
				int k2 = k;
				while (k2 < array.length && array[k2].getValue() == 0) k2++;
				if (k2 < array.length) {
					double lEdge = array[k-1].getValue();
					double rEdge = array[k2].getValue();
					if (lEdge == 0.0 || rEdge == 0.0) {
						MessageCenter.getInstance().put(
								new LogMessage(Level.WARNING, PerstPowerDB.class.getName(),
										methodName, String.format("\nCan't fix load in [%s : %s]. One endpoint is 0. [%f,%f]\n",array[k-1].getDate(),array[k2].getDate(),lEdge,rEdge)));
						return null;
					}
					int    nbPts = (k2-1) - k + 1;
					double slope = (rEdge - lEdge) / nbPts;
					for(int i=k;i < k2;i++) {
						MessageCenter.getInstance().put(
								new LogMessage(Level.WARNING, PerstPowerDB.class.getName(),
										methodName, String.format("\nFixing load at %s from %f to %f  [%f,%f]\n",array[i].getDate(),array[i].getValue(),lEdge + slope * (i-k),lEdge,rEdge)));
						array[i].setValue(lEdge + slope * (i-k));
					}
				} else {
					MessageCenter.getInstance().put(
							new LogMessage(Level.WARNING, PerstPowerDB.class.getName(),
									methodName, String.format("Couldn't find a non-zero value in the entire suffix. Import is bad\n")));
					return null;
				}
			}
		}
		//System.err.println("Adding to " + outFile);
		PerstPowerDB db = new PerstPowerDB(outFile,inc);
		db.open();
		Date t = cal.lastTick(xInc, cal.addSecondsTo(loadData.element().getDate(),-1));
		Date last = cal.lastTick(inc, cal.addSecondsTo(loadData.element().getDate(),-1));
		Date lt;
		int k = 0;
		double v = 0;//loadData.element().getValue();
		boolean q = true;
		db.startTransaction();
		for(LoadData n : loadData){//System.err.println(n);if(true)break;
			t = cal.addSecondsTo(t, xInc);
			lt = cal.lastTick(inc, t);
			v += n.getValue();
			q = q&&n.getQuality();
			k++;
			if(!lt.equals(last)){
				//System.err.println(t+"\t"+v/k + "\t" + k);
				db.addLoadNL("load", t, v/k);
				db.addLoadNL("raw",t, v/k);
				db.addLoadNL("filt",t, v/k);
				v = 0;
				q = true;
				last = lt;
				k = 0;
			}
		}
		db.endTransaction();
		db.close();
		return db;
	}


	@SuppressWarnings("unchecked")
	@Override
	public synchronized void open(){
		_db.open(_table, Storage.DEFAULT_PAGE_POOL_SIZE);
		_fields = (FieldIndex<PerstDataSeries>)_db.getRoot();
		if (_fields == null) {
			_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
            _fields = _db.createFieldIndex(PerstDataSeries.class, "_name", true);
            PerstDataSeries load = new PerstDataSeries("load",_db.
            											createTimeSeries(PerstDataBlock.class,
            															(long)PerstDataBlock.SIZE*1000*_inc*2));
            _fields.put(load);
            PerstDataSeries raw = new PerstDataSeries("raw",_db.
														createTimeSeries(PerstDataBlock.class,
																		(long)PerstDataBlock.SIZE*1000*_inc*2));
			_fields.put(raw);
			PerstDataSeries filt = new PerstDataSeries("filt",_db.
														createTimeSeries(PerstDataBlock.class,
																		(long)PerstDataBlock.SIZE*1000*_inc*2));
			_fields.put(filt);
			PerstDataSeries pred = new PerstDataSeries("pred",_db.
														createTimeSeries(PerstForecastBlock.class,
																		(long)PerstForecastBlock.SIZE*1000*_inc*2));
			_fields.put(pred);
			PerstDataSeries stats = new PerstDataSeries("stats",_db. //this will be a series on one element (just to contain some statistics)
														createTimeSeries(PerstStatsBlock.class,
																		(long)PerstStatsBlock.SIZE*1000*_inc*2));
			_fields.put(stats);
            _db.setRoot(_fields);
            _db.endThreadTransaction();
        }
		/////////////////////////////////
		//A TEMPORARY MEASURE//
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		if(_fields.get("stats")==null){
			PerstDataSeries stats = new PerstDataSeries("stats",_db. //this will be a series of one element per restart
					createTimeSeries(PerstStatsBlock.class,
									(long)PerstStatsBlock.SIZE*1000*_inc*2));
			_fields.put(stats);
		}
		_db.endThreadTransaction();
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		if(_fields.get("raw")==null){
			PerstDataSeries raw = new PerstDataSeries("raw",_db. //this will be a series of one element per restart
					createTimeSeries(PerstDataBlock.class,
									(long)PerstDataBlock.SIZE*1000*_inc*2));
			_fields.put(raw);
		}
		_db.endThreadTransaction();
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		if(_fields.get("filt")==null){
			PerstDataSeries filt = new PerstDataSeries("filt",_db. //this will be a series of one element per restart
					createTimeSeries(PerstDataBlock.class,
									(long)PerstDataBlock.SIZE*1000*_inc*2));
			_fields.put(filt);
		}
		_db.endThreadTransaction();
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		if(_fields.get("pred")==null){
			PerstDataSeries pred = new PerstDataSeries("pred",_db. //this will be a series of one element per restart
					createTimeSeries(PerstForecastBlock.class,
									(long)PerstForecastBlock.SIZE*1000*_inc*2));
			_fields.put(pred);
		}
		_db.endThreadTransaction();
		//////////////////////////////////
		/////////////////////////////////
		_db.setProperty("perst.multiclient.support", Boolean.TRUE);
	}

	@Override
	public synchronized void close(){
		_db.close();
	}

	public synchronized void commit(){
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		_db.commit();
		_db.endThreadTransaction();
	}

	public synchronized Date first(String s){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Date r = _fields.get(s).first();
		_db.endThreadTransaction();
		return r;
	}

	public synchronized Date begin(String s){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Date f = _fields.get(s).first();
		Date r = (f==null)?null:new Date(f.getTime()-_inc*1000);
		_db.endThreadTransaction();
		return r;
	}

	public synchronized Date last(String s){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Date r = _fields.get(s).last();
		_db.endThreadTransaction();
		return r;
	}

	public synchronized Series getLoad(String s, Date st, Date ed)throws Exception{
		Calendar cal = Items.makeCalendar();
		Date strt = cal.beginBlock(_inc,st), end = cal.beginBlock(_inc,ed);
		int inc = _inc*1000;
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Iterator<Tick> i = _fields.get(s).iterator(strt, end);
		int lng = (int)((long)(end.getTime()-strt.getTime())/(long)inc);// + 1;
		double[] load = new double[lng];
		for(int k=lng-1;k>=0;k--)
			load[k] = Double.NaN;
		long off=-1;
		while(i.hasNext()){
			PerstDataPoint p = (PerstDataPoint)i.next();
			p.setTime(cal.lastTick(1, new Date(p.getTime())));
			//System.err.println("Time: "+p.getTime() + "("+new Date(p.getTime())+")\tValue: "+p.getValue());
			off = (p.getTime()-strt.getTime())/inc - 1; // [ldm] -1 makes no sense to me.
			if (off< 0) continue;
			if (off >= load.length) 
				throw new Exception("Writing past the end of the load array");
			load[(int)off] = p.getValue();
		}
		_db.endThreadTransaction();
		return new Series(load,false);
	}
	
	public Series getValues(String s, Date st, Date ed)throws Exception{
		return getLoad(s,st,ed);
	}

	public double getLoad(String s, Date t)throws Exception{
		Calendar cal = Items.makeCalendar();
		return getLoad(s, cal.addSecondsTo(t, -1),t).element(1);
	}
	
	public double getValue(String s, Date t)throws Exception{
		return getLoad(s,t);
	}

	public synchronized void removeBefore(String s, Date t){
		Date tt = new Date(t.getTime()-1);
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		_fields.get(s).remove(first(s), tt);
		_db.endThreadTransaction();
	}
	
	public void addLoadNL(String s, Date time,double load) {
		PerstDataSeries ld = _fields.get(s);
		Calendar cal = Items.makeCalendar();
		Date t = cal.lastTick(1, time);
		if(ld.has(t)){
			ld.remove(t, t);
			ld.add(new PerstDataPoint(t,load));
		} else {
			ld.add(new PerstDataPoint(t,load));
		}
	}
	
	public void fill(String s, Collection<LoadData> set){
		Calendar cal = Items.makeCalendar();
		LinkedList<PerstDataPoint> list = new LinkedList<PerstDataPoint>();
		for(LoadData d:set)
			list.add(new PerstDataPoint(cal.lastTick(1, d.getDate()),d.getValue()));
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		PerstDataSeries ld = _fields.get(s);
		ld.add(list);
		_db.endThreadTransaction();
	}
	public void startTransaction() {
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);		
	}
	public void endTransaction() {
		_db.endThreadTransaction();		
	}
	
	public synchronized void addLoad(String s, Date time, double load){		
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		PerstDataSeries ld = _fields.get(s);
		Calendar cal = Items.makeCalendar();
		Date t = cal.lastTick(1, time);
		if(ld.has(t)){
			ld.remove(t, t);
			ld.add(new PerstDataPoint(t,load));
		}
		else {
			ld.add(new PerstDataPoint(t,load));
		}
		_db.endThreadTransaction();
	}
	
	public void addValue(String s, Date time,double load){
		addLoad(s, time, load);
	}

	public synchronized Series getForecast(Date t) throws Exception {
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Iterator<Tick> i = _fields.get("pred").
			iterator(Items.makeCalendar().addMinutesTo(t, -5), t);
		double[] r = null;
		while(i.hasNext()){
			r = ((PerstForecastPoint)(i.next())).getValue();
		}
		_db.endThreadTransaction();
		if (r!=null)return new Series(r);
		return null;
	}
	
	public double[] getForecastArray(Date t) throws Exception {
		Series s = getForecast(t);
		if(s==null)return null;
		return s.array();
	}
	
	
	public synchronized void addForecast(Date time, Series forecast) throws Exception {
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		PerstDataSeries ld = _fields.get("pred");
		if(ld.has(time)){
			((PerstForecastPoint)(ld.getTick(time))).setValue(forecast.array());
			ld.store();
		}
		else ld.add(new PerstForecastPoint(time,forecast.array()));
		_db.endThreadTransaction();
		
	}
	
	public void addForecastArray(Date time, double[] forecast) throws Exception {
		addForecast(time,new Series(forecast));
	}
	
	public synchronized void addStats(Date ed, int nb, double[] sum, double[] sumP, double[] sos, double[] ovr, double[] und){
		PerstStatsPoint p = new PerstStatsPoint(ed,nb,sum,sumP,sos,ovr,und);
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		PerstDataSeries ld = _fields.get("stats");
		Calendar cal = Items.makeCalendar();
		Date t = cal.lastTick(1, ed);
		if(ld.has(t)){
			ld.remove(t, t);
			ld.add(p);
		}
		else {
			ld.add(p);
		}
		_db.endThreadTransaction();
	}
	
	public synchronized int getNb(){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		PerstDataSeries pds = _fields.get("stats");
		int n = ((PerstStatsPoint)pds.getTick(pds.last())).getNb();
		_db.endThreadTransaction();
		return n;
	}
	
	public synchronized Series getSum() throws Exception{
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		PerstDataSeries pds = _fields.get("stats");
		Series s = new Series(((PerstStatsPoint)pds.getTick(pds.last())).getSum());
		_db.endThreadTransaction();
		return s;
	}
	
	public synchronized Series getSumP() throws Exception{
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		PerstDataSeries pds = _fields.get("stats");
		Series s = new Series(((PerstStatsPoint)pds.getTick(pds.last())).getSumP());
		_db.endThreadTransaction();
		return s;
	}
	
	public synchronized Series getSumOfSquares() throws Exception{
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		PerstDataSeries pds = _fields.get("stats");
		Series s = new Series(((PerstStatsPoint)pds.getTick(pds.last())).getSumOfSquares());
		_db.endThreadTransaction();
		return s;
	}
	
	public synchronized Series getMaxOvr() throws Exception{
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		PerstDataSeries pds = _fields.get("stats");
		Series s = new Series(((PerstStatsPoint)pds.getTick(pds.last())).getMaxOvr());
		_db.endThreadTransaction();
		return s;
	}
	
	public synchronized Series getMaxUnd() throws Exception{
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		PerstDataSeries pds = _fields.get("stats");
		Series s = new Series(((PerstStatsPoint)pds.getTick(pds.last())).getMaxUnd());
		_db.endThreadTransaction();
		return s;
	}
	
	public String toString(){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		StringBuffer s = new StringBuffer("");
		Calendar cal = Items.makeCalendar();
		s.append("RAW:\t");
		PerstDataSeries seq = _fields.get("raw");
		Date prev = begin("raw"), curr;
		if(prev!=null) prev = cal.addYearsTo(prev, -823);
		Iterator<Tick> it = seq.iterator();
		boolean has = false;
		while(it.hasNext() && prev!=null){
			has = true;
			curr = new Date(it.next().getTime());
			if(cal.addSecondsTo(prev, _inc).before(curr)){
				if(!prev.before(begin("raw"))){
					s.append(prev); s.append(",\t");
				}
				s.append(curr);s.append(" -- ");
			}
			prev = curr;
		}
		if(has)
			s.append(prev);
		s.append('\n');
		s.append("FILT:\t");
		seq = _fields.get("filt");
		prev = begin("filt");
		if(prev!=null) prev = cal.addYearsTo(prev, -823);
		it = seq.iterator();
		has = false;
		while(it.hasNext() && prev!=null){
			has = true;
			curr = new Date(it.next().getTime());
			if(cal.addSecondsTo(prev, _inc).before(curr)){
				if(!prev.before(begin("filt"))){
					s.append(prev); s.append(",\t");
				}
				s.append(curr);s.append(" -- ");
			}
			prev = curr;
		}
		if(has)
			s.append(prev);
		s.append('\n');
		s.append("PRED:\t");
		seq = _fields.get("pred");
		prev = begin("pred");
		if(prev!=null) prev = cal.addYearsTo(prev, -823);
		it = seq.iterator();
		 has = false;
		while(it.hasNext() && prev!=null){
			has = true;
			curr = new Date(it.next().getTime());
			if(cal.addSecondsTo(prev, _inc).before(curr)){
				if(!prev.before(begin("pred"))){
					s.append(prev); s.append(",\t");
				}
				s.append(curr);s.append(" -- ");
			}
			prev = curr;
		}
		if(has)
			s.append(prev);
		s.append('\n');
		_db.endThreadTransaction();
		return s.toString();
	}

	public String getInfo() {
		StringBuffer sb = new StringBuffer();
		for(PerstDataSeries ds : _fields) {
			sb.append("serie[");
			sb.append(ds._name).append("]");
			sb.append(" from:").append(ds.first());
			sb.append(" to: ").append(ds.last());
			sb.append(" nbo: ").append(ds.getSize());
			sb.append("\n");
		}
		return sb.toString();
	}
}
