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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.database.PowerDB;
import edu.uconn.vstlf.database.perst.PerstPowerDB;
import edu.uconn.vstlf.shutil.RunTraining;

public class FromTxtToPerst {
	static private void toPerst(String outf, int inc, String inf, Date start) throws Exception
	{
		PowerDB _db = new PerstPowerDB(outf, inc);
		_db.open();
		FileInputStream fs = new FileInputStream(inf);
		DataInputStream in = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		Calendar cal = new Calendar();
		
		String line;
		_db.startTransaction();
		while ((line = br.readLine()) != null) {
			double load = Double.parseDouble(line);
			_db.addLoadNL("filt", start, load);
			_db.addLoadNL("load", start, load);
			_db.addLoadNL("raw", start, load);

			System.err.println("Load on " + start + " stored");
			start = cal.addSecondsTo(start, inc);

		}
		br.close();
		_db.endTransaction();
		_db.close();
	}
	
	static public void main(String[] args)
	{
		if (args.length != 4) {
			System.out.println("USEAGE: ");
		}
		else {
			
			File inf = new File(args[0]);
			File outf = new File(args[1]);
			int inc = Integer.parseInt(args[2]);
			Date startDate = RunTraining.parseDate(args[3]);
			
			if (inc != 4 && inc != 300) {
				System.err.println("The VSTLF System only deals with 4s and 5m data. Set" +
						" the interval to be 4 or 300(5 minute)");
				return;
			}
			
			
			try {

				if (outf.exists()) {
					System.out.println("The perst database " + outf.getName() + " exists. Do you want to add data into it? (Y/n)");
					if(!new BufferedReader(new InputStreamReader(System.in)).readLine().equals("Y")){
						System.out.println("Aborting...");
						return;
					}
				}
			

				toPerst(outf.getPath(), inc, inf.getPath(), startDate);
				System.out.println("End of importing "+inf.getPath());
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
