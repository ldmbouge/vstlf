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

package edu.uconn.vstlf.data;

import java.util.TimeZone;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import edu.uconn.vstlf.config.Items;
public class Calendar extends GregorianCalendar {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8279998698886750363L;
	static final int _yr = java.util.Calendar.YEAR;
	static final int _mo = java.util.Calendar.MONTH;
	static final int _dy = java.util.Calendar.DAY_OF_MONTH;
	static final int _hr = java.util.Calendar.HOUR_OF_DAY;
	static final int _min = java.util.Calendar.MINUTE;
	static final int _sec = java.util.Calendar.SECOND;
	static final int _dow = java.util.Calendar.DAY_OF_WEEK;
	
	public Calendar(String tz){
		super(TimeZone.getTimeZone(tz));
		set(MILLISECOND,getActualMinimum(MILLISECOND));
	}
	
	public Calendar(){
		super(TimeZone.getTimeZone(Items.TimeZone.value()));
		set(MILLISECOND,getActualMinimum(MILLISECOND));
	}
	
	public synchronized Date newDate(int yr, int mo, int dy, int hr, int min, int sec){
		set(_yr, yr);
		set(_mo, mo);
		set(_dy, dy);
		set(_hr, hr);
		set(_min, min);
		set(_sec, sec);
		return new Date(getTime().getTime());
	}
	
	public synchronized DateFormat getDateFormat(String format){
		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(this.getTimeZone());
		return df;
	}
	
	public Date now(){
		return new Date(System.currentTimeMillis());
	}
	
	public synchronized int getYear(java.util.Date d){
		setTime(d);
		return get(_yr);
	}
	
	public synchronized int getMonth(java.util.Date d){
		setTime(d);
		return get(_mo);
	}
	
	public synchronized int getDate(java.util.Date d){
		setTime(d);
		return get(_dy);
	}
	
	public synchronized int getHour(java.util.Date d){
		setTime(d);
		return get(_hr);
	}
	
	public synchronized int getMinute(java.util.Date d){
		setTime(d);
		return get(_min);
	}
	
	public synchronized int getSecond(java.util.Date d){
		setTime(d);
		return get(_sec);
	}
	
	public synchronized int getDayOfWeek(java.util.Date d){
		setTime(d);
		return get(_dow);
	}
	
	public int getDayOfWeek(Day d){
		return getDayOfWeek(beginDay(d));
	}
	
	public synchronized Date addYearsTo(java.util.Date d, int n){
		setTime(d);
		add(_yr,n);
		return new Date(getTime().getTime());
	}
	
	public Day addYearsTo(Day d, int n){
		return dayOf(addYearsTo(beginDay(d),n));
	}
	
		
	public synchronized Date addMonthsTo(java.util.Date d, int n){
		setTime(d);
		add(_mo,n);
		return new Date(getTime().getTime());
	}
	
	public Day addMonthsTo(Day d, int n){
		return dayOf(addMonthsTo(beginDay(d),n));
	}
	
		
	public synchronized Date addDaysTo(java.util.Date d, int n){
		setTime(d);
		add(_dy,n);
		return new Date(getTime().getTime());
	}
	
	public Day addDaysTo(Day d, int n){
		return dayOf(addDaysTo(beginDay(d),n));
	}
	
	
	public synchronized Date addHoursTo(java.util.Date d, int n){
		setTime(d);
		add(_hr,n);
		return new Date(getTime().getTime());
	}
	
	public synchronized Date addMinutesTo(java.util.Date d, int n){
		assert(d!=null);
		setTime(d);
		add(_min,n);
		return new Date(getTime().getTime());
	}
	
	public synchronized Date addSecondsTo(java.util.Date d, int n){
		setTime(d);
		add(_sec,n);
		return new Date(getTime().getTime());
	}
	
	String dowString(int dow){
		String r;
		switch(dow){
		case MONDAY : r = "Mon";break;
		case TUESDAY : r = "Tue";break;
		case WEDNESDAY : r = "Wed";break;
		case THURSDAY : r = "Thu";break;
		case FRIDAY : r = "Fri";break;
		case SATURDAY : r = "Sat";break;
		case SUNDAY : r = "Sun";break;
		default : r = "???";break;
		}
		return r;
	}
	
	public synchronized String string(java.util.Date d){
		setTime(d);
		return 			 get(_yr) + " - " +
						 get(_mo) + " - " +
						 get(_dy) + " " +
						 get(_hr) + ":" +
						 get(_min) + ":" +
						 get(_sec);
	}
	/**
	 * Get the beginning of a sz seconds block (aligned: i.e., 12:00, 12:05, 12:10,....
	 * @param sz the number of seconds in a block
	 * @param d  the time within the block (we want to get the beginning of it)
	 * @return the time at the beginning of the block. For instance for 12:37:42 we would get back: 12:35
	 */
	public Date beginBlock(int sz,java.util.Date d){
		int size = sz*1000;
		long t = d.getTime();
		return new Date(t - (t%size)); 
	}
	
	public Date lastTick(int sz, java.util.Date d){
		int size = sz*1000;
		long t = d.getTime();
		return new Date(t - (t%size));
	}
	
	/**
	 * This returns the end of an sz seconds block that contains time d.
	 * @param sz  the length of the block in seconds
	 * @param d the time identifying a block
	 * @return the end time of the block.
	 */
	public Date endBlock(int sz, java.util.Date d){
		int size = sz*1000;
		long t = d.getTime();
		return new Date(t - (t%size) + size);
	}
	
	public synchronized Date beginDay(java.util.Date t){
		setTime(t);
		set(_hr,getActualMinimum(_hr));
		set(_min,getActualMinimum(_min));
		set(_sec,getActualMinimum(_sec));
		return new Date(getTime().getTime());
	}
	
	public Date endDay(java.util.Date d){
		return addDaysTo(beginDay(d),1);
	}
	
	public Date beginDay(Day d){
		return newDate(d._yr,d._mo,d._dy,0,0,0);
	}
	
	public Date endDay(Day d){
		return addDaysTo(beginDay(d),1);
	}
	
	public synchronized Date beginHour(java.util.Date t){
		setTime(t);
		set(_min,getActualMinimum(_min));
		set(_sec,getActualMinimum(_sec));
		return new Date(getTime().getTime());
	}
	
	public synchronized Date beginMinute(java.util.Date t){
		setTime(t);
		set(_sec,getActualMinimum(_sec));
		return new Date(getTime().getTime());
	}
	
	public Day dayOf(java.util.Date d){
		return new Day(getYear(d),getMonth(d),getDate(d));
	}	
}
