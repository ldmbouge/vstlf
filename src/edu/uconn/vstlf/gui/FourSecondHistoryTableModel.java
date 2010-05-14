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

import java.util.Date;


class FourSecondHistoryTableModel extends HistoryTableModel implements IHistoryFrame
{

  public FourSecondHistoryTableModel()
   {
		super();
		setRowCount(0 );

	}

   public void updateTableData(Date at, double val)
   {
		setRowCount(getRowCount() + 1);
		setValueAt(at, getRowCount()-1, TIME_COLUMN);
		setValueAt(val, getRowCount()-1, VALUE_COLUMN);

   }
   public void updateTableFilteredData(Date at, double oldValue, double NewVal)
   {
		// Update the filtered column for the specified date

   }

   public void updateTableMissingData(Date at)
   {
		// Change the color of the time column date cell for this missing date

   }

}

