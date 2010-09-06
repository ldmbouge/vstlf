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

import edu.uconn.vstlf.data.message.DummyMsgHandler;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.data.message.VSTLFMsgLogger;

public class RunAudition {

	
	public static void main(String[] args) {
		try
		{
			MessageCenter.getInstance().setHandler(new VSTLFMsgLogger("vstlf.log", new DummyMsgHandler()));
			MessageCenter.getInstance().init();
			edu.uconn.vstlf.batch.Audition.main(args);
			MessageCenter.getInstance().put(new VSTLFMessage(VSTLFMessage.Type.EOF));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
