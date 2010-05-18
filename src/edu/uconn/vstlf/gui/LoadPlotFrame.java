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

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

import javax.swing.*;

import edu.uconn.vstlf.gui.PlotPanel.Curve;

public class LoadPlotFrame extends JPanel {
	private PlotPanel _pane;
	private JPanel    _ctrl;
	private int xOfs = 100,yOfs = 100;
	/**
	 * 
	 */
	private static final long serialVersionUID = -6143142848956299884L;

	LoadPlotFrame() {
		//super("Load Plot",true,false,true,true);
		//super("Load Plot");
		//hide();
		_pane = new PlotPanel();
		_ctrl = new JPanel();
		//_pane.setPreferredSize(new Dimension(500,200));
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER,_pane);
		add(BorderLayout.LINE_END,_ctrl);		
		//this.setContentPane(_pane);		
        setSize(500,300);
        setLocation(xOfs, yOfs);
		_ctrl.setLayout(new GridLayout(13,1));
		for(final Curve c : Curve.values()) {
			Checkbox cb = new Checkbox(c.toString(),c == Curve.actual);
			cb.setForeground(PlotPanel._colors[c.ordinal()]);
			cb.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
				    int state = e.getStateChange();
			    	_pane.enableCurve(c,state == ItemEvent.SELECTED);
				}
			});
			_pane.enableCurve(c,cb.getState());
			_ctrl.add(cb);
		}
		this.setDoubleBuffered(true);
	}
	public void addPoint(Date at, double[] values,double actual) {
		_pane.addPoint(at, values,actual);
	}
}
