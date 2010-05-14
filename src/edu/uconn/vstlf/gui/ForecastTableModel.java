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

import javax.swing.table.DefaultTableModel;
import java.util.Date;

import edu.uconn.vstlf.data.Calendar;

class ForecastTableModel extends DefaultTableModel implements IForecastFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3629166105820658577L;
	int _numRows;
	boolean _init = false;
	Calendar _cal;

  public ForecastTableModel()
   {
		super();
		setRowCount( 12 );  // 12 five minute forecast values
		_cal = new Calendar("America/New_York");

	}

   public int getColumnCount()
   {
      return COLUMN_NAMES.length;
   }

   public Class<?> getColumnClass(int col)
   {
      switch (col)
      {

         case TIME_COLUMN:
            return Date.class;
         case FORECAST_COLUMN:
            return Double.class;
         case ACTUAL_COLUMN:
        	return Double.class;
         case DIFF_COLUMN:
        	 return Double.class;
         default:
            return Object.class;
      }
   }

   public String getColumnName(int column)
   {
      return COLUMN_NAMES[column];
   }

   public void clear()
   {

      if (_numRows > 0)
      {
         fireTableRowsDeleted(0, _numRows - 1);
         fireTableRowsUpdated(0, _numRows - 1);
      }

   }


   public boolean isCellEditable(int row, int col)
   {
      switch (col)
      {
         default:
            return false;
      }
   }

   public void updateTableData(Date at, double[] valA,double[] valP)
   {
	   for (int i=0; i<valP.length; i++) {
		   Date d = _cal.addMinutesTo(at, 5*(i+1));
		   setValueAt(d, i, TIME_COLUMN);
		   //double actVal = (i<valA.length)? valA[i]:3.0/0.0;
		   setValueAt(valA[i],i,ACTUAL_COLUMN);
		   setValueAt(valP[i], i, FORECAST_COLUMN);
		   setValueAt(Math.abs(valA[i] - valP[i]),i,DIFF_COLUMN);

	   }

   }

}

