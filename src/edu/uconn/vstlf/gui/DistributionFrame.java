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

import java.awt.Font;
import java.awt.Dimension;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

class DistributionFrame extends ForecastFrame implements IDistributionFrame
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 5897639208579780830L;
	final static int _xOffset = /*67*/5, _yOffset = 5;

   public DistributionFrame(DefaultTableModel model, String titleBar)
   {
		super(model, titleBar);
		//setSize(200, 200);
		//setLocation(_xOffset, _yOffset);
		this.setPreferredSize(new Dimension(512,230));
   }
	void setupJTable()
	{
		_table.setFont(new Font("Courier",  Font.PLAIN, 12));
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// custom cell renderer for Forecast column
		//setIntegerRenderer(MINUTES_COLUMN);
		setDecimalRenderer(MAE_COLUMN);
		setDecimalRenderer(MAPE_COLUMN);
		setDecimalRenderer(MAXOVER_COLUMN);
		setDecimalRenderer(MAXUNDR_COLUMN);
		setDecimalRenderer(DEV_COLUMN);
	}
}
