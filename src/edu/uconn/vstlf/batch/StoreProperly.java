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

package edu.uconn.vstlf.batch;
import java.io.File;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.database.perst.*;
public class StoreProperly {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			String rawFile = args[0];
			String filtFile = args[1];
			String predFile = args[2];

			if(!new File(".vstlf").delete())System.out.println("Couldn't delete '.vstlf'");
			PerstPowerDB raw = new PerstPowerDB(rawFile, 300);
			raw.open();
			PerstPowerDB filt = new PerstPowerDB(filtFile, 300);
			filt.open();
			PerstForecastDB pred = new PerstForecastDB(predFile);
			pred.open();
			PerstPowerDB dst = new PerstPowerDB(".vstlf",300);
			dst.open();
			dst.startTransaction();
			Calendar cal = new Calendar();
			
			java.util.Date beg = raw.begin("load") , end;
			Series r = raw.getLoad("load", beg,raw.last("load"));
			for(int i = 1; i<= r.length();i++)
				dst.addLoadNL("raw", cal.addMinutesTo(beg,5*i), r.element(i));
			System.out.println("Did raw");
			
			beg = filt.begin("load");
			r = filt.getLoad("load", beg,filt.last("load"));
			for(int i = 1; i<= r.length();i++)
				dst.addLoadNL("filt", cal.addMinutesTo(beg,5*i), r.element(i));
			System.out.println("Did filt");
			
			beg = pred.first(); end = pred.last();
			for(java.util.Date t = beg; !t.after(end);t = cal.addMinutesTo(t, 5))
				dst.addForecast(t, pred.getForecast(t));
			System.out.println("Did pred");
			
			dst.endTransaction();
			dst.close();
			filt.close();
			raw.close();
			pred.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
