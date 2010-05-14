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
import edu.uconn.vstlf.data.Calendar;

import java.util.Date;


import java.util.LinkedList;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import org.xml.sax.SAXParseException;
import java.text.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class ParseLoadData
{
	LinkedList<LoadData> historyData = new LinkedList<LoadData>();
	LoadData currentData = null;
	SimpleDateFormat format =  null;
	int historyInterval = 300;
	Calendar cal ;
	public ParseLoadData()
	{
        this.format =  new SimpleDateFormat("M/dd/yyyy h:mm:ss a");
        this.cal = new Calendar("America/New_York");
	}
	public void setHistoryInterval(int in)
	{
		this.historyInterval = in;
	}
	public int getHistoryInterval()
	{
		return this.historyInterval;
	}
	public void parseData(String fileName)
		throws Exception
	{

		Document xmlDoc = DOMUtil.parse(fileName);
		NodeList nodeList = xmlDoc.getElementsByTagName("data");
		//System.out.println("Parsing Complete.  Found " + nodeList.getLength() + " <data> nodes in file '" + fileName + "'");
		// note there should only be one <data> node in the XML file that contains both the current data and 24 hours of five min data
		for(int i=0; i<nodeList.getLength(); i++){
		  Node childNode = nodeList.item(i);
		  NamedNodeMap attrs = childNode.getAttributes();
		  if(attrs.getNamedItem("id").getNodeValue().equals("ld_ne")){
			  this.currentData = new LoadData(getLoadValue(attrs), true, getLoadTime(attrs));
			  processRawHistory(childNode.getTextContent(), attrs);
		  }
		}
	}
	double getLoadValue(NamedNodeMap nodeMap)
		throws NumberFormatException
	{
		Node currentDataValueNode = nodeMap.getNamedItem("val");
		String nodeVal = currentDataValueNode.getNodeValue();
		return getLoadValue(nodeVal.trim());
	}
	double getLoadValue(String value)
		throws NumberFormatException
	{
		return Double.parseDouble(value.trim());
	}
	Date getLoadTime(NamedNodeMap nodeMap)
		throws ParseException
	{
		Node currentDataTimeNode = nodeMap.getNamedItem("c");
		String nodeVal = currentDataTimeNode.getNodeValue();
		return this.format.parse(nodeVal.trim());

	}
	protected void processRawHistory(String rawHistory, NamedNodeMap nodeMap)
		throws NumberFormatException, ParseException
	{
		historyData = new LinkedList<LoadData>();
		if (rawHistory == "")
			return;

		// get history start/end times
		Node historyStartNode = nodeMap.getNamedItem("hs");
		String nodeVal = historyStartNode.getNodeValue();
		Date historyStart = this.format.parse(nodeVal);
		Node historyEndNode = nodeMap.getNamedItem("he");
		nodeVal = historyEndNode.getNodeValue();
		// split up the raw csv history data
		//Date historyEnd = this.format.parse(nodeVal);
		String[] splitStringArr = rawHistory.split(",");
		// calculate the number of seconds between each history value (should be 300 sec)
		//float interval = ((historyEnd.getTime() - historyStart.getTime())/1000) / splitStringArr.length;
		Date startTime = this.cal.beginMinute(historyStart);
		System.out.println("Start time = " + startTime.toString());
		int minute = cal.getMinute(startTime);
		System.out.println("Minutes = " + minute + ", adding " + minutesToAdd(minute));
		startTime = this.cal.addMinutesTo(startTime, 5 - (minute % 5));

		//System.out.println("History Interval = " + interval);
		long start = historyStart.getTime();
		for (int i=splitStringArr.length-1; i>=0; i--) {
			//System.out.println("History[" + i + "] =" + splitStringArr[i]);

			historyData.add(new LoadData(getLoadValue(splitStringArr[i].trim()),  true,  startTime));
			startTime = this.cal.addSecondsTo(startTime, this.historyInterval);
		}



	}
	int minutesToAdd(int min)
	{
		int interval = this.historyInterval/60;
		int returnVal = interval - (min % interval);
		if (returnVal == interval)
			returnVal = 0;

		return returnVal;

	}
	public LinkedList<LoadData> getHistory()
	{
		return this.historyData;
	}
	public LoadData getCurrentData()
	{
		return this.currentData;
	}

	static public void main(String[] in)
	{
		if (in.length != 1 ) {
			System.out.println("java ParseLoadData <fileName>");
			System.exit(0);
		}
		try {
			ParseLoadData d = new ParseLoadData();
			d.parseData(in[0]);
			System.out.println("Current Load Data = " + d.getCurrentData().toString());

			LinkedList<LoadData> loadData = d.getHistory();
			Iterator<LoadData> iter = loadData.iterator();
			while (iter.hasNext()) {
				System.out.println(iter.next().toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}


}