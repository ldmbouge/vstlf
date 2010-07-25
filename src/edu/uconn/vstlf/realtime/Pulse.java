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

package edu.uconn.vstlf.realtime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class Pulse {
	private int	  _milli;
    private Timer timer;
    private final PulseAction _action;
    private final Calendar _strt;
    public Pulse(int milli,PulseAction action,Date st)  {
    	_milli = milli;
    	_action = action;
    	Calendar cal = new GregorianCalendar();
    	_strt = new GregorianCalendar();
    	_strt.setTime(st);
    	start(cal);
    }

    public void start(Calendar startAt) {
    	timer = new Timer();
    	Date now = startAt.getTime();
    	//System.out.format("Pulsing from: %s\n",now.toString());
    	timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	//System.out.println("TICK\n");
            	_strt.add(Calendar.MILLISECOND, _milli);
            	boolean goOn = _action.run(_strt.getTime());
                if (!goOn)
                	timer.cancel();
            }
        },now , _milli);
    }

    public static void main(String[] args) {
        new Pulse(4000,new PulseAction() {
        	public boolean run(Date at) {
                System.out.println("4 seconds up " + at.toString());
                return true;
        	}
        },new Date());
    }
    public void stop() {
    	timer.cancel();
    }
}