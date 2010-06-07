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

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;

class HistoryTableModel extends ForecastTableModel implements IHistoryFrame
{

  /**
	 * 
	 */
	private static final long serialVersionUID = 756625670419244670L;

	public HistoryTableModel()
   {
		super();
		setRowCount( 12 * 12 );  // 12 five minute history values * 12 hours

	}

   public int getColumnCount()
   {
      return HISTORY_COLUMN_NAMES.length;
   }


   public String getColumnName(int column)
   {
      return HISTORY_COLUMN_NAMES[column];
   }

   public Class<?> getColumnClass(int col)
   {
      switch (col)
      {

         case TIME_COLUMN:
            return Date.class;
         case VALUE_COLUMN:
         case FILTERED_COLUMN:
            return Double.class;
         default:
            return Object.class;
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

   public void updateTableData(Date at, double[] val)
   {
		// MATT:  Pls goto IsoVstlfGui and correctly call this method (two places)
	   for (int i=0; i<val.length; i++) {
		   Date d = _cal.addMinutesTo(at, -5*(i));
		   setValueAt(d, i, TIME_COLUMN);
		   setValueAt(val[i], i, VALUE_COLUMN);
	   }
   }
   public void updateTableFilteredData(Date at, double oldValue, double NewVal)
   {
	   Calendar cal = Items.makeCalendar();
	   Date d = (Date)getValueAt(0,TIME_COLUMN);
	   int i = 0;
	   while(!d.equals(at) && i<25){
		   i++;
		   d = cal.addMinutesTo(d, -5);
	   }
	   setValueAt(NewVal,i,FILTERED_COLUMN);
   }

   public void updateTableMissingData(Date at)
   {
		// MATT:  Change the color of the time column date cell for this missing date

   }

}

