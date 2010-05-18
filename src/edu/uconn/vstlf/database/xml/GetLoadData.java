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
import java.util.LinkedList;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.database.PowerDB;
import java.util.Iterator;

public class GetLoadData {
	ParseLoadData loadParser;
	Calendar cal;
	int historyInterval = 300;

	public GetLoadData() {
		loadParser = null;
		cal = null;
	}
	public void setHistoryInterval(int in)	{ historyInterval = in;}
	public int getHistoryInterval()         { return historyInterval;}
	public void getData(String file, boolean url) throws Exception {
		if (url)
			file = FileDownload.download(file);
		this.loadParser = new ParseLoadData(file);
		this.loadParser.setHistoryInterval(this.historyInterval);
	}

	public LinkedList<LoadData> getHistory() {
		return this.loadParser.getHistory();
	}
	public LoadData getCurrentData() {
		return this.loadParser.getCurrentData();
	}
	
	public void fillPowerDb(LinkedList<LoadData> history, PowerDB db) {
		Iterator<LoadData> iter = history.iterator();
		db.startTransaction();
		while (iter.hasNext()) {
			LoadData load = iter.next();
			db.addLoadNL("raw",load.getDate(), load.getValue());
			db.addLoadNL("filt",load.getDate(), load.getValue());
			
		}
		db.endTransaction();
	}
}