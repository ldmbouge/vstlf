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

import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class AddDBContent {
	public static void main(String[] args) throws Exception
	{
		if (args.length != 4) {
			System.out.println("USAGE: AddDBContent <inDBName> <outDBName> <interval> <datatype>");
			System.exit(0);
		}
		
		String indbName = args[0];
		String outdbName = args[1];
		int interval = Integer.parseInt(args[2]);
		String [] types = args[3].split(",");
		
		if (interval != 4 && interval != 300) {
			System.err.println("The VSTLF System only deals with 4s and 5m data. Set" +
				" the interval to be 4 or 300(5 minute)");
			System.exit(0);
		}

		PerstPowerDB indb = new PerstPowerDB(indbName, interval);
		PerstPowerDB outdb = new PerstPowerDB(outdbName, interval);
		indb.open();
		outdb.open();
		
		outdb.startTransaction();
		Calendar cal = new Calendar();
		for (int i = 0; i < types.length; ++i) {
			String loadType = types[i];
			Date st = cal.addSecondsTo(indb.first(loadType), -interval);
			Date ed = indb.last(loadType);
			
			System.out.println("Load starts from " + indb.first(loadType) + " to " + ed);
			Series load = indb.getLoad(loadType, st, ed);
			Date curLoadDate = indb.first(loadType);
			for (int t = 1; t <= load.length(); ++t) {
				outdb.addLoadNL(loadType, curLoadDate, load.element(t));
				System.out.println("Add load on " + curLoadDate);
				curLoadDate = cal.addSecondsTo(curLoadDate, interval);
			}
		}
		outdb.endTransaction();

		outdb.close();
		indb.close();
		
		System.out.println("Completed adding loads");
	}
}
