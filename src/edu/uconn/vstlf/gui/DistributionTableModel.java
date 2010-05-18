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

class DistributionTableModel extends ForecastTableModel implements IDistributionFrame {

  /**
	 * 
	 */
	private static final long serialVersionUID = -4299026705803404218L;

	public DistributionTableModel()
   {
		super();
		setRowCount( 12);  // 12 five minute history values * 12 hours
		for(int i = 0;i<12;i++)
			setValueAt(5*(i+1), i, MINUTES_COLUMN);

	}

   public int getColumnCount()
   {
      return DISTRIBUTION_COLUMN_NAMES.length;
   }


   public String getColumnName(int column)
   {
      return DISTRIBUTION_COLUMN_NAMES[column];
   }

   public Class<?> getColumnClass(int col)
   {
      switch (col)
      {

         case MINUTES_COLUMN:
            return Integer.class;
         default:
            return Double.class;
      }
   }

   public void setMean(double[] mape, double[] mae){
	   for (int i=0; i<12; i++) {
		   setValueAt(mape[i],i,MAPE_COLUMN);
		   setValueAt(mae[i], i, MAE_COLUMN);
	   }
   }
   public void setDev(double[] dev){
	   for (int i=0; i<12; i++) {
		   setValueAt(dev[i], i, DEV_COLUMN);
	   }
   }
   
   public void setMaxOvr(double[] max){
	   for (int i=0; i<12; i++) {
		   setValueAt(max[i], i, MAXOVER_COLUMN);
	   }
   }
   
   public void setMaxUndr(double[] max){
	   for (int i=0; i<12; i++) {
		   setValueAt(max[i], i, MAXUNDR_COLUMN);
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

   

}

