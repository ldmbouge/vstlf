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

import java.util.Date;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.message.DummyMsgHandler;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.data.message.VSTLFMsgLogger;
import edu.uconn.vstlf.preprocessing.Produce5MLoad;

public class RunGenerate5M {
	
	static public void main(String[] args)
	{
		if (args.length != 2 && args.length != 4) {
			System.out.println("USAGE: \n\tjava -jar uconn-vstlf.jar gen5m <indbName> <outdbName>   or"+
	 "\n \tjava -jar uconn-vstlf.jar gen5m <indbName> <outdbName> \"<startDate yyyy/MM/dd>\" \"<endDate yyyy/MM/dd>\"");
		}
		else {
			
			try {
				MessageCenter.getInstance().setHandler(new VSTLFMsgLogger("vstlf.log", new DummyMsgHandler()));
				MessageCenter.getInstance().init();
				
				String indbName = args[0], outdbName = args[1];
				Produce5MLoad producer = new Produce5MLoad(indbName, outdbName, Items.makeCalendar(),
						"load", new String[]{"load", "raw", "filt"});
				if (args.length == 2) {
					producer.execute();
				} 
				else {
					Date st = RunTraining.parseDate(args[2]);
					Date ed = RunTraining.parseDate(args[3]);
					producer.execute(st, ed);
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
