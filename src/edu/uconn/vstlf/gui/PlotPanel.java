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

package edu.uconn.vstlf.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.event.*;
import java.util.Date;
import java.util.EnumSet;
import java.util.Vector;
import java.text.*;
import javax.swing.event.*;
import com.web_tomorrow.utils.suntimes.*;

import edu.uconn.vstlf.data.Calendar;

public class PlotPanel extends Canvas implements MouseInputListener{
	
	static Color[] _colors = {Color.BLACK,Color.RED,Color.blue,Color.GREEN,Color.CYAN,
							  Color.GRAY,Color.MAGENTA,Color.ORANGE,Color.PINK,Color.YELLOW,
							  Color.DARK_GRAY,Color.BLACK,Color.blue};

	class Plot {
		double[] _data;
		int      _nowOfs;
		final Color[] _color = PlotPanel._colors;
		int      _cid;
		private boolean _visible;
		Plot(int cid) {
			_cid = cid;
			_data = new double[24 * 60 / 5];
			for(int k=0;k<_data.length;k++) 
				_data[k] = 0.0;
			_nowOfs = 0;
			_visible = false;
		}
		Color getColor() { return _color[_cid];}
		void addPoint(double val) {
			_data[_nowOfs] = val;
			_nowOfs = (_nowOfs+1 == _data.length) ? 0 : (_nowOfs+1);			
		}
		double minOf(double minv) {
			for(int k=0;k< _data.length;k++) {
				if (_data[k]==0.0) continue;
				minv = Math.min(_data[k],minv);
			}			
			return minv;
		}
		double maxOf(double maxv) {
			for(int k=0;k< _data.length;k++) {
				if (_data[k]==0.0) continue;
				maxv = Math.max(_data[k],maxv);
			}			
			return maxv;
		}
		double valueAt(int x) { return _data[x];}
		int length() { return _data.length;}
		boolean isVisible() { return _visible;}
		void setVisible(boolean b) { _visible = b;}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 6191227181180819348L;
	private Calendar _cal;
	enum Curve {actual,s5,s10,s15,s20,s25,s30,s35,s40,s45,s50,s55,s60};
	Vector<Plot> _model;
	Date _now;
	Date _paint;
	double _minv = Double.MAX_VALUE;
	double _maxv = 0;
	int _mouseX = 0;
	int _mouseY = 0;
	NumberFormat _format;
	
	PlotPanel() {
		super();
		_cal = new Calendar();
		_model = new Vector<Plot>();
		for(Curve c : Curve.values()) {
			_model.add(c.ordinal(),new Plot(c.ordinal()));
		}
		_now = new Date();
		this.setPreferredSize(new Dimension(500,200));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		//this.setDoubleBuffered(true);
		_format = new DecimalFormat();
		_format.setMaximumFractionDigits(1);
		_format.setGroupingUsed(false);
	}
	public void update(Graphics g) {
		super.update(g);
	}
	
	public void paint(Graphics g) {
		Dimension d = this.getSize();
		//System.out.format("repainting... %d x %d\n",d.width,d.height);
		super.paint(g);		
		Graphics2D g2d = (Graphics2D)g;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, d.width, d.height);
		double ry = _maxv - _minv;
		ry = ry == 0 ? 1.0 : ry;
		AffineTransform tx = new AffineTransform();
		tx.translate(0, d.height);
		tx.scale(1,-1);
		tx.scale(((double)d.width)/(5*288),((double)d.height)/ry);
		tx.translate(0, - _minv);	
		//g2d.setTransform(tx);
		int fromPos = _model.get(Curve.actual.ordinal())._nowOfs - 23 * 12;
		fromPos = fromPos < 0 ? (24 * 12 + fromPos) : fromPos;
		double xDelta = 5;
		
		
		Date sd = _cal.addHoursTo(_now, -23);		
		
		//Begin painting sunsets/////////////////////////
		g2d.setColor(Color.RED);
		double ppt = (((double)d.width)/(5*288));//pix per minute
		Calendar gmt = new Calendar("GMT");
		int yesterYear = gmt.getYear(sd);
		int yesterMonth = gmt.getMonth(sd);
		int yesterDay = gmt.getDate(sd);
		int toYear = gmt.getYear(_now);
		int toMonth = gmt.getMonth(_now);
		int toDay = gmt.getDate(_now);
		try{
			Time yesterZenith = SunTimes.getSunsetTimeUTC(yesterYear, yesterMonth+1, yesterDay, -72.6166667, 42.2041667, SunTimes.CIVIL_ZENITH);
			Time toZenith = SunTimes.getSunsetTimeUTC(toYear, toMonth+1, toDay, -72.6166667, 42.2041667, SunTimes.CIVIL_ZENITH);
			Date yz = gmt.newDate(yesterYear, yesterMonth, yesterDay, yesterZenith.getHour(), yesterZenith.getMinute(), yesterZenith.getSecond());
			Date tz = gmt.newDate(toYear, toMonth, toDay, toZenith.getHour(), toZenith.getMinute(), toZenith.getSecond());
			int ym = 0; int tm = 0;
			while(_cal.addMinutesTo(sd, ym).before(yz))
				ym++;
			while(_cal.addMinutesTo(sd, tm).before(tz))
				tm++;
			if(ym*ppt > 0)
				g2d.drawLine((int)(ym*ppt), 0, (int)(ym*ppt), (int)d.getHeight());
			if(tm*ppt < d.getWidth())
				g2d.drawLine((int)(tm*ppt), 0, (int)(tm*ppt), (int)d.getHeight());
			//System.out.println(ym+"\t"+tm);
		}
		catch(Exception e){}
		//End painting sunsets////////////////////////////////
		
		g2d.setColor(Color.LIGHT_GRAY);
		//Date sd = _cal.addHoursTo(_now, -23);		
		for(int p=0;p<24;p++) {
			double x1b = (int) ((p * 12 -1) * xDelta);
			double x1 = (int) (x1b  * (((double)d.width)/(5*288)));
			double y2 = d.getHeight(); // _maxv;
			g2d.drawLine((int)x1,0,(int)x1,(int)y2);
		}
		g2d.setFont(new Font("Courier",Font.PLAIN, 10));
		g2d.setColor(Color.BLACK);
		for(int p=0;p<24;p++) {
			int nh = _cal.getHour(sd);
			int nm = _cal.getMinute(sd);
			double x1b = (int) ((p * 12 -1) * xDelta);
			double x1 = (int) (x1b  * (((double)d.width)/(5*288)));
			String lbl = String.format("%02d:%02d",nh,nm);
			g2d.drawString(lbl, (int)x1, (int)d.getHeight());			
			sd = _cal.addHoursTo(sd, 1);
		}
		g2d.setColor(Color.GRAY);
		Line2D curs = new Line2D.Float(0,_mouseY,this.getWidth(),_mouseY);
		int y = this.getHeight() - _mouseY;
		double frac = 1.0*y/this.getHeight();
		double load = frac*(_maxv-_minv)+_minv;
		frac = ((double)_mouseX)/this.getWidth();
		int idx = (int)(frac*288);
		int below = (_model.get(Curve.actual.ordinal()).valueAt((fromPos+idx)%288)>load)?-2:1;
		if(_mouseY<15){
			below = -2;
		}
		if(_mouseY+10>this.getHeight()){
			below = 1;
		}
		int mouseX = _mouseX - 100;
		if(mouseX < 0){
			mouseX += 100;
			if (below < 0){
				mouseX += 12;
			}
		}			
		g2d.drawString(String.format("Load:%sMW", _format.format(load)), mouseX, _mouseY -( 5*below));
		g2d.draw(curs);
		for(Curve c: Curve.values()) {
			if (_model.get(c.ordinal()).isVisible())
				plotCurve(g2d,tx,fromPos,c);
		}	
		//_paint = when;
	}
	void plotCurve(Graphics2D g2d,AffineTransform tx,int fromPos,Curve c) {
		//System.out.format("Plotting %s...\n",c);
		double x= 5 + c.ordinal()*5;
		double xDelta = 5;
		Path2D.Double path = new Path2D.Double();
		Plot vec = _model.get(c.ordinal());
		boolean hasPt = false;
		for(int k=0;k< 23*12 - 1;k++) {
			int xOfs = (fromPos + k) % vec.length();
			double ord = vec.valueAt(xOfs);
			if (hasPt) {
				if (ord==0)
					path.moveTo(x,ord);
				else path.lineTo(x, ord);
			} else {
				if (ord!=0) {
					path.moveTo(x,ord);
					hasPt =true;
				}
			}
			x += xDelta;
		}
		g2d.setColor(vec.getColor());
		Shape ts = tx.createTransformedShape(path);
		//System.out.println(ts.toString());
		g2d.draw(ts);
	}
	
	void addPoint(Date at,double[] values,double actual) {
		_now = at;
		for(Curve c: EnumSet.complementOf(EnumSet.of(Curve.actual))) {
			Plot vec = _model.get(c.ordinal());	
			double theVal = values[c.ordinal()-1];
			_minv = Math.min(_minv, theVal);
			_maxv = Math.max(_maxv, theVal);
			vec.addPoint(theVal);
			//System.out.format("Added point [%s] on [%s] @ %f\n",at,c,values[c.ordinal()-1]);
		}
		_model.get(Curve.actual.ordinal()).addPoint(actual);
		this.repaint();
	}
	public void enableCurve(Curve c,boolean b) {
		_model.get(c.ordinal()).setVisible(b);
		this.repaint();
	}
	
	
	
	/*
	 * MouseListener Methods
	 */
	
	public void mouseEntered(MouseEvent e){
		
	}
	
	public void mouseExited(MouseEvent e){
		
	}
	
	public void mouseMoved(MouseEvent e){
		_mouseX = e.getX();
		_mouseY = e.getY();
		//System.out.format("Mouse at %d/%d\n",_mouseX,_mouseY);
		repaint();
	}
	
	public void mousePressed(MouseEvent e){}
	
	public void mouseReleased(MouseEvent e){}
	
	public void mouseClicked(MouseEvent e){}
	
	public void mouseDragged(MouseEvent e){}
	
}
