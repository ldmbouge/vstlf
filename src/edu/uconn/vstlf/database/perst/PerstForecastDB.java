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
import org.garret.perst.*;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.doubleprecision.Series;
public class PerstForecastDB{
	
	Storage _db;
	String _table;
	FieldIndex<PerstForecastSeries> _fields;
	
	public PerstForecastDB(String table){
		_db = StorageFactory.getInstance().createStorage();
		_db.setProperty("perst.multiclient.support", Boolean.TRUE);
		_table = table;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void open() throws Exception {
		_db.open(_table, 32*1024*1024);
		_fields = (FieldIndex<PerstForecastSeries>)_db.getRoot();
		if (_fields == null) { 
			_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
            _fields = _db.createFieldIndex(PerstForecastSeries.class, "_name", true);
            PerstForecastSeries fs = new PerstForecastSeries("forecasts",_db.
            											createTimeSeries(PerstForecastBlock.class, 
            															 (long)PerstForecastBlock.SIZE*4000*2));
            _fields.put(fs);
            _db.setRoot(_fields);
            _db.endThreadTransaction();
        }
		_db.setProperty("perst.multiclient.support", Boolean.TRUE);
	}
	
	public synchronized void close() throws Exception {
		_db.close();
	}
	
	public synchronized void commit() throws Exception{
		_db.beginThreadTransaction(Storage.READ_WRITE_TRANSACTION);
		_db.commit();
		_db.endThreadTransaction();
	}
	
	public synchronized Date first(){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Date r = new Date(_fields.get("forecasts").first().getTime());
		_db.endThreadTransaction();
		return r;
	}
	
	public synchronized Date begin(){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Date r = new Date(_fields.get("forecasts").first().getTime()-300*1000);
		_db.endThreadTransaction();
		return r;
	}
	
	
	public synchronized Date last(){
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Date r = new Date(_fields.get("forecasts").last().getTime());
		_db.endThreadTransaction();
		return r;
	}

	public synchronized Series getForecast(Date t) throws Exception {
		_db.beginThreadTransaction(Storage.READ_ONLY_TRANSACTION);
		Iterator<PerstForecastPoint> i = _fields.get("forecasts").
			iterator(Items.makeCalendar().addMinutesTo(t, -5), t);
		double[] r = null;
		while(i.hasNext()){
			r = i.next().getValue();
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
		PerstForecastSeries ld = _fields.get("forecasts");
		if(ld.has(time)){
			ld.getTick(time).setValue(forecast.array());
			ld.store();
		}
		else ld.add(new PerstForecastPoint(time,forecast.array()));
		_db.endThreadTransaction();
		
	}
	
	public void addForecastArray(Date time, double[] forecast) throws Exception {
		addForecast(time,new Series(forecast));
	}
	
	
	
	
}
