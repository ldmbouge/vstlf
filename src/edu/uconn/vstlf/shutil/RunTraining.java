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

package edu.uconn.vstlf.shutil;

import java.io.*;
import java.util.Date;
import edu.uconn.vstlf.database.perst.*;

public class RunTraining {

	static String _USAGE = "USAGE:\n\tjava -jar uconn-vstlf.jar train <lowBank> <highBank> <xmlFile> "+
	 "\n\n\t\t lowBank, highBank in [0,11] : the program will train ANN banks for the offsets in the specified interval" +
	 "\n\t\t xmlFile : 5minute load file.  XML.  (see manual for XML format) \n\n" +
	 "\tThe specified set of neural network banks will be trained over the time period contained in 'xmlFile'.\n" +
	 "\tIt is assumed that the current directory contains a folder called 'anns/'.  If the contents\n\n\t\t" +
	 "(some subset of {bank0.ann, bank1.ann, bank2.ann, ... , bank11.ann})\n\n\t" +
	 "include the '.ann' files corresponding to the set of banks to be trained, then the existing networks will be used as a\n\t" +
	 "starting point for further training.\n\n";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		edu.uconn.vstlf.data.Calendar cal = new edu.uconn.vstlf.data.Calendar("America/New_York");
		String xmlFile = null; int lo=0, hi=0; Date st, ed;
		if (args.length != 3) { 					//check # of args
			System.out.println(_USAGE);
			System.exit (-1);
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
			System.exit(-2);
		}
		catch(FileNotFoundException e){
			System.out.println("'" + e.getMessage() + "' does not refer to real file.");
			System.exit(-2);
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
			System.out.println("Extracting 5m load signal from "+ xmlFile);
			PerstPowerDB ppdb = PerstPowerDB.fromXML(tfName, 300, xmlFile);
			ppdb.open();
			st = cal.endDay(ppdb.first("filt"));
			ed = cal.beginDay(ppdb.last("filt"));
			ppdb.close();
			if(cal.addYearsTo(st, 1).before(ed)){
				System.out.println("WARNING:  The file '"+xmlFile+"' contains less than one year of data.\n" +
						"It is recommended that you use at least one continuous year of load signal to train your neural nets.\n" +
						"Would you like to continue the training on these limited data? (Y/n)");
				if(!new BufferedReader(new InputStreamReader(System.in)).readLine().equals("Y")){
					System.out.println("Aborting...");
					System.exit(0);
				}
				else
					System.out.println("If you insist...");
			}
			System.out.println("Training about to commence.  This will take about "+(3*(hi-lo+1))+
					" hours.  Please confirm that you would like to continue. (Y/n)");
			if(!new BufferedReader(new InputStreamReader(System.in)).readLine().equals("Y")){
				System.out.println("Aborting...");
				System.exit(0);
			}
			edu.uconn.vstlf.batch.VSTLFTrainer.train(tfName, st, ed, lo, hi);
			System.out.println("Training Complete");
		} catch (Exception e) {
		    System.out.println(e.toString());
		    e.printStackTrace();
		    System.exit(0);
		}
	}

}
