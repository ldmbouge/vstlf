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
import edu.uconn.vstlf.database.perst.PerstPowerDB;

import java.util.Iterator;
import java.io.File;

public class GetLoadData
{
	ParseLoadData loadParser = new ParseLoadData();
	Calendar cal;
	int historyInterval = 300;

	public GetLoadData()
	{
		this.loadParser = new ParseLoadData();
		this.loadParser.setHistoryInterval(this.historyInterval);
	}
	public void setHistoryInterval(int in)
	{
		this.historyInterval = in;
	}
	public int getHistoryInterval()
	{
		return this.historyInterval;
	}
	public void getData(String file, boolean url)
		throws Exception
	{
		if (url)
			file = FileDownload.download(file);

		this.loadParser.parseData(file);

	}

	public LinkedList<LoadData> getHistory()
	{
		return this.loadParser.getHistory();
	}
	public LoadData getCurrentData()
	{
		return this.loadParser.getCurrentData();
	}
	
	/*
	public void createPowerDb(LinkedList<LoadData> history, String rawFive, String filterFive)
	{
		File f = new File(rawFive);
		if(f.exists()) if(!f.delete()) {System.err.println("???"); System.exit(0);}
		f = new File(filterFive);
		if(f.exists()) if(!f.delete()) {System.err.println("???"); System.exit(0);}

		PowerDB perst = new PerstPowerDB(rawFive, this.historyInterval);
		PowerDB perst1 = new PerstPowerDB(filterFive, this.historyInterval);
		perst.open();
		perst1.open();
		Iterator<LoadData> iter = history.iterator();
		while (iter.hasNext()) {
			//System.out.println(iter.next().toString());
			LoadData load = iter.next();
			perst.addLoad(load.getDate(), load.getValue());
			perst1.addLoad(load.getDate(), load.getValue());

		}
		System.out.println("TIMES\t\t"+perst.begin()+"\t\t"+perst.last());
		System.out.println("TIMES\t\t"+perst1.begin()+"\t\t"+perst1.last());
		perst.close();
		perst1.close();
	}//*/

	public void fillPowerDb(LinkedList<LoadData> history, PowerDB db)
	{
		Iterator<LoadData> iter = history.iterator();
		db.startTransaction();
		while (iter.hasNext()) {
			LoadData load = iter.next();
			db.addLoadNL("raw",load.getDate(), load.getValue());
			db.addLoadNL("filt",load.getDate(), load.getValue());
			
		}
		db.endTransaction();
	}

	/*
	public static void main(String[] args)
	{
		try {
			GetLoadData loadData = new GetLoadData();
			// get current data
			loadData.getData("current_data.xml", false);
			System.out.println("Current Load:  " + loadData.getCurrentData().toString());
			loadData.getData("24_hour_data.xml", false);
			loadData.createPowerDb(loadData.getHistory(), "iso5MinLoadRaw.pod", "iso5MinLoadFiltered.pod");
		}
		catch (Exception e) {
			e.printStackTrace();

		}
	}//*/
	
}