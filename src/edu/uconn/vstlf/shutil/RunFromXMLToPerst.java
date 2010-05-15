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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Vector;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class RunFromXMLToPerst {
	
	static public Vector<File> listFiles(File dir) {
		Vector<File> files = new Vector<File>();
		if (dir.isDirectory()) {
			File [] subFiles = dir.listFiles();
			for (int i = 0; i < subFiles.length; ++i) {
				File f = subFiles[i];
				if (f.isFile())
					files.add(f);
				else if (f.isDirectory())
					files.addAll(listFiles(f));
			}
		}
		else
			files.add(dir);
		
		return files;
	}
	
	static public void main(String[] args)
	{
		if (args.length != 3) {
			System.out.println("USEAGE: \n\tjava -jar uconn-vstlf.jar build-perst <xmlFileName> <perstDBName> <incremental interval(4 or 300)>");
		}
		else {
			
			File inf = new File(args[0]);
			File outf = new File(args[1]);
			int inc = Integer.parseInt(args[2]);
			
			if (inc != 4 && inc != 300) {
				System.err.println("The VSTLF System only deals with 4s and 5m data. Set" +
						" the interval to be 4 or 300(5 minute)");
				System.exit(0);
			}
			
			if (!inf.exists()) {
				System.err.println("The input file or directory (" + inf.getName() + " does not exist!");
				System.exit(0);
			}
			
			try {

				if (outf.exists()) {
					System.out.println("The perst database " + outf.getName() + " exists. Do you want to add data into it? (Y/n)");
					if(!new BufferedReader(new InputStreamReader(System.in)).readLine().equals("Y")){
						System.out.println("Aborting...");
						System.exit(0);
					}
				}
			
				Vector<File> xmlFiles = listFiles(inf);

				for (int i = 0; i < xmlFiles.size(); ++i) {
					PerstPowerDB.fromXML(outf.getName(), inc, xmlFiles.elementAt(i).getPath());
					System.out.println("End of importing "+xmlFiles.elementAt(i).getPath());
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
