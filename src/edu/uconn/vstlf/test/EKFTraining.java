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

import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.message.DummyMsgHandler;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.data.message.VSTLFMsgLogger;
import edu.uconn.vstlf.database.perst.*;
import edu.uconn.vstlf.config.Items;

public class EKFTraining {

	static String _USAGE = "USAGE:\n\tjava -jar uconn-vstlf.jar train <lowBank> <highBank> <xmlFile>   or"+
	 "\n \tjava -jar uconn-vstlf.jar train <lowBank> <highBank> <xmlFile> \"<startDate yyyy/MM/dd>\" \"<endDate yyyy/MM/dd>\""+
	 "\n\n\t lowBank, highBank in [0,11]:\tthe program will train ANN banks for\n\t\t\t\t\tthe offsets in the specified interval" +
	 "\n\n\t xmlFile:\t\t\t5minute load file.  XML.\n\t\t\t\t\t(see manual for XML format)" +
	 "\n\n\t startDate, endDate:\t\tthe training period\n\n" + 
	 "\tThe specified set of neural network banks will be trained over the\ntime period contained in 'xmlFile'." +
	 "It is assumed that the current\ndirectory contains a folder called 'anns/'.  If the contents\n\n\t" +
	 "(some subset of {bank0.ann, bank1.ann, bank2.ann, ... , bank11.ann})\n\n" +
	 "include the '.ann' files corresponding to the set of banks to be trained,\nthen the existing networks will be used as a\n" +
	 "starting point for further training.\n\n";
	
	public static Date parseDate(String str) {
		try {
			DateFormat df = Items.makeCalendar().getDateFormat("yyyy/MM/dd - HH:mm:ss");
			Date d = df.parse(str);
			return d;
		}
		catch (Exception e) {
		}
		
		try {
			DateFormat df = Items.makeCalendar().getDateFormat("yyyy/MM/dd");
			Date d = df.parse(str);
			return d;
		}
		catch (Exception e) {
		}
		
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Calendar cal = Items.makeCalendar();
		Date t0 = cal.now();
		System.out.println("EKF Training starting from " + t0);
		
		String xmlFile = null; int lo=0, hi=0; Date st, ed;
		if (args.length != 3 && args.length != 5) { 					//check # of args
			System.out.println(_USAGE);
			return;
		}
		try{										//assign args
			xmlFile = args[2];
			if(!new File(xmlFile).exists())
				throw new FileNotFoundException(xmlFile);
			lo = Integer.parseInt(args[0]);
			hi = Integer.parseInt(args[1]);
			if(lo > hi || lo < 0 || hi > 11)
				throw new NumberFormatException();
		}
		catch(NumberFormatException e){
			System.out.println("Specified set of ANN banks is not valid. (lo > hi || lo < 0 || hi > 11)");
			return;
		}
		catch(FileNotFoundException e){
			System.out.println("'" + e.getMessage() + "' does not refer to real file.");
			return;
		}
		try {								//run stuff
			String tfName = ".load5m.pod";
			File tempFile;
			while(true){
				tempFile = new File(tfName);
				if(tempFile.exists())
					tfName = "." + tfName;
				else
					break;
			}
			tempFile.deleteOnExit();
			
			//get file extension name
			String ext = xmlFile.substring(xmlFile.lastIndexOf('.')+1).toLowerCase();
			
			PerstPowerDB ppdb;
			if (ext.equals("xml")) {
				System.out.println("Extracting 5m load signal from "+ xmlFile);
				ppdb = PerstPowerDB.fromXML(tfName, 300, xmlFile);
			}
			else if (ext.equals("pod")) {
				ppdb = new PerstPowerDB(xmlFile, 300);
				tfName = xmlFile;
			}
			else 
				throw new Exception("Don't accept data source with '" + ext + "' as extension");
			ppdb.open();
			st = cal.endDay(ppdb.first("filt"));
			ed = cal.beginDay(ppdb.last("filt"));
			ppdb.close();
			if(cal.addYearsTo(st, 1).after(ed)){
				System.out.println("WARNING:  The file '"+xmlFile+"' contains less than one year of data.\n" +
						"It is recommended that you use at least one continuous year of load signal to train your neural nets.\n" +
						"Would you like to continue the training on these limited data? (Y/n)");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line = br.readLine();
				if(line ==null || !line.equals("Y")){
					System.out.println("Aborting...");
					return;
				}
				else
					System.out.println("If you insist...");
			}
			System.out.println("Training about to commence.  This will take about "+(3*(hi-lo+1))+
					" hours.  Please confirm that you would like to continue. (Y/n)");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = br.readLine();
			if(line ==null || !line.equals("Y")){
				System.out.println("Aborting...");
				return;
			}
			if (args.length == 5) {
				Date strt = parseDate(args[3]);
				Date end = parseDate(args[4]);
				if (strt == null)
					System.err.println("Cannot parse date " + args[3]);
				else if (end == null)
					System.err.println("Cannot parse date " + args[4]);
				else {
					st = strt;
					ed = end;
				}
			}
			MessageCenter.getInstance().setHandler(new VSTLFMsgLogger("vstlf.log", new DummyMsgHandler()));
			MessageCenter.getInstance().init();
			edu.uconn.vstlf.batch.VSTLFTrainer.trainEKFANNs(tfName, st, ed, lo, hi, 10);
			MessageCenter.getInstance().put(new VSTLFMessage(VSTLFMessage.Type.EOF));
			System.out.println("Training Complete");
			
			System.out.println("EKF Training ends at " + cal.now() + "(starting form " + t0 + ")");
		} catch (Exception e) {
		    System.out.println(e.toString());
		    e.printStackTrace();
		    return;
		}
	}

}
