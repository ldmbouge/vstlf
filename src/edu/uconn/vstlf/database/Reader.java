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
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.database.perst.*;

public class Reader {

	public static void main(String[] args) {
		try{
			System.out.format("#arguments: %d\n",args.length);
			for(int k=0;k < args.length;k++)
				System.out.format("arg[%d] = %s\n",k,args[k]);
			String name = args[0];
			int inc = Integer.parseInt(args[1]);
			Calendar cal = new Calendar("America/New_York");
			Date st;     // = cal.newDate(2009, 0, 1, 0, 0, 0);
			Date ed;    // = cal.newDate(2009, 3, 1, 0, 0, 0);
			PowerDB pdb = new PerstPowerDB(name,inc);
			pdb.open();
			System.out.println("'"+name+"' contains("+pdb.begin("load")
                                              +", "+pdb.last("load")+"]");
			st = pdb.begin("load");
			ed = pdb.last("load");
			Series load = pdb.getLoad("filt",st, ed);
			pdb.close();
			Date cur = (Date)st.clone();
			for(int i = 1;i<=load.length();i++){				
				double cl = load.element(i);
				if (cl < 1000)
					System.out.format("%7d  %s load:%f\n",i,cur,cl);
				cur = cal.addMinutesTo(cur, 5);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
