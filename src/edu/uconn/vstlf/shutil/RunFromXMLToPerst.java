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
import java.util.Vector;

import edu.uconn.vstlf.data.message.DummyMsgHandler;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.data.message.VSTLFMsgLogger;
import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class RunFromXMLToPerst {
	
	static public Vector<File> listFiles(File dir) {
		Vector<File> files = new Vector<File>();
		if (dir.isDirectory()) {
			File [] subFiles = dir.listFiles();
			for (int i = 0; i < subFiles.length; ++i) {
				File f = subFiles[i];
				
				if (f.isFile() && !f.getName().endsWith("~"))
					files.add(f);
				else if (f.isDirectory())
					files.addAll(listFiles(f));
			}
		} else
			files.add(dir);
		
		return files;
	}
	
	static public void main(String[] args)
	{
		if (args.length != 3) {
			System.out.println("USAGE: \n\tjava -jar uconn-vstlf.jar build-perst <xmlFileName> <perstDBName> <incremental interval(4 or 300)>");
		}
		else {
			
			File inf = new File(args[0]);
			File outf = new File(args[1]);
			int inc = Integer.parseInt(args[2]);
			
			if (inc != 4 && inc != 300) {
				System.err.println("The VSTLF System only deals with 4s and 5m data. Set" +
						" the interval to be 4 or 300(5 minute)");
				return;
			}
			
			if (!inf.exists()) {
				System.err.println("The input file or directory (" + inf.getPath() + ") does not exist!");
				return;
			}
			
			try {

				if (outf.exists()) {
					System.out.println("The perst database " + outf.getName() + " exists. Do you want to add data into it? (Y/n)");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String line = br.readLine();
					if(line == null ||  !line.equals("Y")){
						System.out.println("Aborting...");
						return;
					}
				}

				MessageCenter.getInstance().setHandler(new VSTLFMsgLogger("vstlf.log", new DummyMsgHandler()));
				MessageCenter.getInstance().init();

				Vector<File> xmlFiles = listFiles(inf);

				for (int i = 0; i < xmlFiles.size(); ++i) {
					PerstPowerDB theDB = PerstPowerDB.fromXML(outf.getName(), inc, xmlFiles.elementAt(i).getPath());
					if (theDB!=null) {
						System.out.print(".");
						System.out.flush();
					} else {
						System.err.println("\nFailed importing "+xmlFiles.elementAt(i).getPath());
						break;
					}
				}
				
				//MessageCenter.getInstance().put(new VSTLFMessage(VSTLFMessage.Type.EOF));
				MessageCenter.getInstance().put(new VSTLFMessage(VSTLFMessage.Type.StopMessageCenter));
				MessageCenter.getInstance().join();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
