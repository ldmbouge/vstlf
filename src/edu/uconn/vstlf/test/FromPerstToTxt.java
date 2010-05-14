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

package edu.uconn.vstlf.test;


import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.PerstPowerDB;
import edu.uconn.vstlf.shutil.RunTraining;

public class FromPerstToTxt {
	static private void toText(String outf, int inc, String inf, Date start, Date end, String loadType, int min) throws Exception
	{
		PowerDB db = new PerstPowerDB(inf, inc);
		db.open();
		FileOutputStream fs = new FileOutputStream(outf);
		PrintStream ps = new PrintStream(fs);
		
		Calendar cal = new Calendar("America/New_York");
		Date st = cal.addSecondsTo(start, -inc);
		Date ed = end;
		
		Series load = db.getLoad(loadType, st, ed);
		Date curLoadDate = start;
		ps.println(loadType + ",\t\t\tDate");
		for (int t = 1; t <= load.length(); ++t) {
			int loadmin = cal.getMinute(curLoadDate);
			if (loadmin == min) {
				ps.print(load.element(t) + ",\t\t\t" + curLoadDate + "\n");
			}
			curLoadDate = cal.addSecondsTo(curLoadDate, inc);
		}
		System.out.println("Complete outputing data on " + min + "m " + loadType + " from " + start + " to " + end);
		db.close();
	}
	
	public static void main(String[] args) throws Exception
	{
		if (args.length != 7) {
			System.out.println("USAGE: FromPerstToTxt <inDBName> <outFileName> <interval> <datatype> <start> <end> <miniute>");
			System.exit(0);
		}
		
		String indbName = args[0];
		String outfileName = args[1];
		int interval = Integer.parseInt(args[2]);
		String loadType = args[3];
		Date start = RunTraining.parseDate(args[4]), end = RunTraining.parseDate(args[5]);
		int min = Integer.parseInt(args[6]);
		
		if (interval != 4 && interval != 300) {
			System.err.println("The VSTLF System only deals with 4s and 5m data. Set" +
				" the interval to be 4 or 300(5 minute)");
			System.exit(0);
		}
		
		toText(outfileName, interval, indbName, start, end, loadType, min);
	}
}
