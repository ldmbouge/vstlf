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
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.database.perst.*;
public class XMLTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			//PerstPowerDB db = PerstPowerDB.fromXML("test.pod", 300, "xml/isone-load-Q1-2007-one-min.xml");
			//			 db = PerstPowerDB.fromXML("test.pod", 300, "xml/isone-load-Q2-2007-one-min.xml");
			//			 db = PerstPowerDB.fromXML("test.pod", 300, "xml/isone-load-Q3-2007-one-min.xml");
			//PerstPowerDB db = new PerstPowerDB("test.pod",300);
			PerstPowerDB db = PerstPowerDB.fromXML("4s_2009_Mar12-17.pod",4,"xml/iso-load-four-sec-3-12-17-2009.xml");
			db.open();
			System.out.println(db.begin("raw"));
			System.out.println(db.last("raw"));
			Series load = db.getLoad("raw",db.begin("raw"), db.last("raw"));
			System.out.println(load.length());
			System.out.println(load.suffix(12));
			System.out.println(load.prefix(12));
			db.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
