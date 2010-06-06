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

package edu.uconn.vstlf.database;
import java.util.Date;

import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.database.perst.*;

public class Loader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			Calendar cal = new Calendar();
			Date st = cal.newDate(2008, 5, 1, 0, 0, 0);
			Date ed = cal.newDate(2009, 5, 6, 0, 0, 0);
			String name = "4s_Jun2008-9.pod";
			int inc = 4;
			
			PerstPowerDB pdb = null;
			for(Date d = st;d.before(ed);d = cal.addDaysTo(d, 1)){
				System.out.println("Transfering a(nother) day from "+d);
				String inFile = "xml/four-sec-"+(cal.getMonth(d)+1)+"-"+(cal.getDate(d))+"-"+(cal.getYear(d))+".xml";
				pdb = PerstPowerDB.fromXML(name, inc, inFile);
			}
			pdb = new PerstPowerDB(name,inc);
			pdb.open();
			System.out.println("'"+name+"' contains ("+pdb.begin("raw")+", "+pdb.last("raw")+"]");
			//Series load = pdb.getLoad(pdb.begin(), pdb.last());
			pdb.close();
			//for(int i = 1;i<=load.length();i++){
			//	System.out.println(i+"\t"+load.element(i));
			//}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
