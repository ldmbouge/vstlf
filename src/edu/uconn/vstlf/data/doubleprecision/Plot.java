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

package edu.uconn.vstlf.data.doubleprecision;

import javax.swing.*;
import java.awt.*;

public class Plot extends JPanel {
	
	Series _s;
	double _lo,_up;
	
	public Plot(Series s,String title)throws Exception{
		this(s,
			 title,
			 new MinFunction().imageOf(s.array(false))-10,
			 new MaxFunction().imageOf(s.array(false))+10);
		}
	public Plot(Series s,String title, double lo, double up)throws Exception{
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		//	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}catch(Exception e){e.printStackTrace();}
		_s = s;
		_lo = lo;
		_up = up;
		JFrame win = new JFrame(title);
		win.setLocation(32, 32);
		this.setBackground(Color.white);
		win.setSize(256, 128);
		win.add(this);
		win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		win.setVisible(true);
		win.repaint();
	}
	
	public int length(){return _s.length();}
	public double element(int i)throws Exception{return _s.element(i);}
	public double getLo(){return _lo;}
	public double getUp(){return _up;}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		try{
			int btm = this.getHeight();
			NormalizingFunction nOrd = new NormalizingFunction(_lo,_up,0,btm);
			NormalizingFunction nAbs = new NormalizingFunction(0,length(),0,this.getWidth());
			g.setColor(Color.black);
			for(int i = 1;i<=length();i++){
				int x1 = (int)Math.floor(nAbs.imageOf(i));
				int x0 = (int)Math.floor(nAbs.imageOf(i-1));
				int y1 = (int)Math.floor(nOrd.imageOf(element(i)));
				int y0 = (int)Math.floor(nOrd.imageOf(element(i-1)));
				y1 = btm - y1;
				y0 = btm - y0;
				g.drawLine(x0, y0, x1, y1);
				g.fillOval(x1, y1, 2, 2);
			}
			
		}catch(Exception e){System.err.println("Cannot Plot:  " + e); e.printStackTrace();}
	}

}
