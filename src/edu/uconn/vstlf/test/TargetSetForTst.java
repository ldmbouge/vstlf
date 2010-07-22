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

import java.util.Date;

import org.junit.Test;

import edu.uconn.vstlf.batch.VSTLFTrainer;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.database.perst.PerstPowerDB;
import edu.uconn.vstlf.shutil.RunTraining;

public class TargetSetForTst {
	@Test public void test() throws Exception
	{
		PerstPowerDB db = new PerstPowerDB("5m_2007-Feb2010.pod", 300);
		db.open();
		Calendar cal = new Calendar();
		Date t = RunTraining.parseDate("2009/3/19 - 10:05:00");
		VSTLFTrainer.targetSetFor(t, cal, db);
		db.close();
	}
}
