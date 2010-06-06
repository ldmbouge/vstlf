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

import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.data.doubleprecision.*;
import edu.uconn.vstlf.database.perst.*;

import java.io.File;
import java.util.Date;

public class Audition {
	
	static String audit(Calendar cal,PerstPowerDB db, Date first, Date last, int mon, int hr, double thresh, boolean smry, boolean hist) throws Exception{
		StringBuffer string = new StringBuffer("\n\n");
		
		
		Function mae = new MAEFunction();
		Function mape = new MAPEFunction();
		Function sqrt = new SqrtFunction();
		Function sqr = new SquaringFunction();
		Function maxi = new MaxFunction();
		Function mini = new MinFunction();
		
		Series sum = new Series(12);
		Series sumP = new Series(12);
		Series sos = sum;
		Series und = sum;
		Series ovr = sum;
		
		int[][] hists = new int[12][36];
		
		int nb = 0;
		Date st = db.first("pred");
		Date ed = db.last("pred");
		if(first.after(st)) st = first;
		if(last.before(ed)) ed = last;
		for(Date t = st;!t.after(ed);t = cal.addMinutesTo(t, 5)){
				Series a = db.getLoad("filt", t, cal.addHoursTo(t, 1));
				Series p = db.getForecast(t);
				if(p!=null && a.countOf(Double.NaN)==0 && (mon == 0 || cal.getMonth(t)==mon-1) && (hr==0 || cal.getHour(t)==hr-1)){
					Series err = mae.imageOf(a,p);
					Series dif = p.minus(a);
					Series errP = mape.imageOf(a,p);
					sum = sum.plus(err);
					sumP = sumP.plus(errP);
					sos = sos.plus(sqr.imageOf(err));
					ovr = maxi.imageOf(ovr,dif);
					und = mini.imageOf(und,dif);
					
					if(maxi.imageOf(err.array(false))>thresh){
						System.out.println(t);
						System.out.println("actual:\t"+a);
						System.out.println("forecast:\t"+p);
						System.out.println();
					}
					nb++;
					for(int i = 1;i<=12;i++){
						if(err.element(i)>=360)
							hists[i-1][35]++;
						else
							hists[i-1][(int)(err.element(i)/10)]++;
					}
			}
		}
		string.append(nb); string.append(" forecasts assessed.\n\n");
		Function df = new DivisorFunction(nb);
		Series ave = df.imageOf(sum);
		Series aveP = df.imageOf(sumP);
		if(smry){
			string.append("\t\t\t5min\t\t10min\t\t15min\t\t20min\t\t25min\t\t30min\t\t35min\t\t40min\t\t45min\t\t50min\t\t55min\t\t1hour\n");
			string.append("MAPE:\t\t"); string.append(aveP); string.append('\n');
			string.append("MAE:\t\t"); string.append(ave); string.append('\n');
			Series dev = sqrt.imageOf(df.imageOf(sos).minus(sqr.imageOf(ave)));
			string.append("StD (Error):\t"); string.append(dev); string.append('\n');
			string.append("Max OverError:\t"); string.append(ovr); string.append('\n');
			string.append("Max UnderError:\t"); string.append(und); string.append('\n');
			string.append('\n');string.append('\n');
		}
		if(hist){
			for(int i = 0;i<12;i++){
				string.append(((i+1)*5)+"minutes out\n");
				for(int j=0;j<36;j++){
					string.append("   "); string.append(j*10); string.append(':');string.append('\t');
					for(int n=0;n<(hists[i][j]/(nb/200));n++)
						string.append('-');
					string.append('\n');
				}
				string.append('\n');string.append('\n');
			}
		}
		return string.toString();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			//if(args.length < 1)return;
			String name = ".vstlf";
			int mon = 0;
			int hr = 0;
			int thresh = 10000;
			
			boolean hist = false, smry = false;
			
			for(int i = 0;i<args.length;i++){
				String val = args[i].substring(1);
				String p = args[i].substring(0, 1);
				if(p.equals("m"))
					mon = Integer.parseInt(val);
				if(p.equals("h"))
					hr = Integer.parseInt(val);
				if(p.equals("e"))
					thresh = Integer.parseInt(val);
				if(p.equals("g"))
					hist = true;
				if(p.equals("s"))
					smry = true;
				if(p.equals("n"))
					name = val;
				if(p.equals("!"))
					break;
			}
			
			
			if(new File(name).exists()){
				PerstPowerDB db = new PerstPowerDB(name,300);
				db.open();
				Calendar cal = new Calendar();
				System.out.println("History File Contents: \n\n");
				System.out.println(db);
				System.out.println("\n");
				
				Date first = db.first("filt");
				Date last = cal.addHoursTo(db.last("pred"), -1);
				if(hist || smry){
					String audit = audit(cal,db,first,last,mon, hr, thresh, smry, hist);
					System.out.println(audit);
				}
				db.close();
			}
			else{
				System.out.format("[history file '%s' does not exist]\n\n", name);
			}
			
			
			if(args.length < 1)
				printOptions();
		}
		catch(NumberFormatException nf){
			System.out.println("Values of parameters should be integers");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void printOptions(){
		System.out.println("Possible command line arguments:\n\t\t(You're seeing this because there were no args.)");
		
		System.out.println("\t'n<name>'\tIndicates the name of the history file to audit.");
		System.out.println("\t\t\t\t(default is '.vstlf')\n");
		
		System.out.println("\t's'\t\tIndicates that a stats table summarizing historical");
		System.out.println("\t\t\t\terror should be shown.\n");
		
		System.out.println("\t'g'\t\tIndicates error histograms (ASCII ART) should be");
		System.out.println("\t\t\t\t included in output.\n");
		
		System.out.println("\t'm<#0-12>'\tIndicates a month.  Only data from that month");
		System.out.println("\t\t\t\tappear in error summary.(default is 0, meaning ");
		System.out.println("\t\t\t\tthatall months are included)\n");
		
		System.out.println("\t'h<#0-24>'\tIndicates an hour of the day.  Only data from");
		System.out.println("\t\t\t\tthat hour appear in error summary. (default");
		System.out.println("\t\t\t\tis 0, meaning that all hours are included)\n");
		
		System.out.println("\t'e<#>'\t\tIndicates the error threshold above which the");
		System.out.println("\t\t\t\trelated forecast is detailed in output.");
		System.out.println("\t\t\t\t(use with 's' or 'g')(default is 10000)\n");
		
		System.out.println("\t'!'\t\tIgnores all subsequent arguments.  (good for suppressing");
		System.out.println("\t\t\t\t this usage statement, among other things.)\n");
		
		System.out.println("\n");
	}

}
