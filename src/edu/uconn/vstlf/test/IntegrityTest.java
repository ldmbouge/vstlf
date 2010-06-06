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

public class IntegrityTest {

	public static void main(String[] args) throws Exception
	{
		if (args.length != 3) {
			System.out.println("USAGE: IntegrityTest <dbName> <interval> <datatype>");
			System.exit(0);
		}
		
		String dbName = args[0];
		int interval = Integer.parseInt(args[1]);
		String [] types = args[2].split(",");
		
		PerstPowerDB db = new PerstPowerDB(dbName, interval);
		db.open();
		
		for (int i = 0; i < types.length; ++i)
			checkIntegrity(db, types[i], interval);
		db.close();
	}
	
	static public Boolean checkIntegrity(PerstPowerDB db, String loadType, int inc) throws Exception
	{
		Calendar cal = new Calendar();
		Date st = cal.addSecondsTo(db.first(loadType), -inc);
		Date ed = db.last(loadType);
		System.out.println("Load starts from " + db.first(loadType) + " to " + ed);
		Series load = db.getLoad(loadType, st, ed);
		Boolean integ = true;
		int gapStart = -1, gapEnd = -1;
		for (int i = 1; i <= load.length(); ++i) {
			Double d = new Double(load.element(i));
			if (d.equals(Double.NaN)) {
				if (gapStart == -1) {
					gapStart = i;
					gapEnd = i;
				}
				else 
					++gapEnd;
				integ = false;
			}
			else {
				if (gapStart != -1) {
					System.err.println("Load from " + cal.addSecondsTo(st, gapStart*inc) + 
							" to " + cal.addSecondsTo(st, gapEnd*inc) + " does not exist");
					gapStart = -1;
					gapEnd = -1;
				}
			}
		}
		
		if (gapStart != -1) {
			System.err.println("Load from " + cal.addSecondsTo(st, gapStart*inc) + 
					" to " + cal.addSecondsTo(st, gapEnd*inc) + " does not exist");
		}
		
		if (!integ)
			System.err.println("The database contains gaps at some points");
		else 
			System.out.println("The database contains loads from " + db.first(loadType) + " to " + ed);
		return integ;
	}
}
