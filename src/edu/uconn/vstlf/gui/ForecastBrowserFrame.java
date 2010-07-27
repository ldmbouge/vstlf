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
import java.util.Arrays;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.*;
import edu.uconn.vstlf.database.*;

public class ForecastBrowserFrame extends JPanel implements IForecastFrame,ListSelectionListener
{
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1748606677073832076L;
	final static int  _xOffset = 400, _yOffset = 5;
	   JTable _table = null; 
	   DefaultListModel _lstmodel = null;
	   JList _list = null;
	   TableRowSorter<TableModel> sorter = null;
	   PowerDB _db;
	   Calendar _cal;
	   public ForecastBrowserFrame(DefaultTableModel model, String titleBar, PowerDB db)
	   {
		/*super(titleBar,
			  true, //resizable
			  false, //closable
			  false, //maximizable
			  true);//iconifiable
			  */
		setFocusable(true);
		_cal = Items.makeCalendar();
		_db = db;
		_table = new JTable(model);
	    sorter = new TableRowSorter<TableModel>(model);
	    _table.setRowSorter(sorter);
		setupJTableAndList();
				
		//Create the scroll panes and add their contents to them.
		JScrollPane lstPane = new JScrollPane(_list);
		//lstPane.setPreferredSize(new Dimension(200,430));
		JScrollPane tblPane = new JScrollPane(_table);
		//Add the scroll panes to this window.
		//setLayout(new FlowLayout());
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		add(lstPane);
		add(tblPane);
		// Set the frame characteristics
		setSize(750, 430);
		//Set the window's location.
		setLocation(_xOffset, _yOffset);
	   }
	   	   	 
	   public void addDate(Date t){
		   _lstmodel.addElement(t);
		   if(_lstmodel.getSize()>288)
			   _lstmodel.remove(0);
		   if (_list.getSelectedIndex()==-1)
			   _list.setSelectedIndex(_lstmodel.getSize()-1);
		   valueChanged(null);//new ListSelectionEvent(new Object(),0,0,false));
	   }
	   
	   public void valueChanged(ListSelectionEvent e){
		   Date t = (Date)_list.getSelectedValue();
		   if(t==null)
			   return;
		   ForecastTableModel ftm = ((ForecastTableModel)(_table.getModel()));
		   double[] valP = null, valA=null;
		   try{
			   valP = _db.getForecastArray(t);
			   valA = _db.getLoad("filt",t, _cal.addHoursTo(t, 1)).array(false);
		   }
		   catch(Exception x){
			   System.out.print(Arrays.toString(valP));
			   System.out.print(Arrays.toString(valA));
			   x.printStackTrace();
			   valP = valA = new double[12];
		   }
		   ftm.updateTableData(t, valA,valP);
	   }
		void setupJTableAndList()
		{			
			_lstmodel = new DefaultListModel();
			_list = new JList(_lstmodel);
			_list.setFixedCellWidth(200);
			//_list.setPreferredSize(new Dimension(200,430));
			_list.addListSelectionListener(this);
					
			_table.setFont(new Font("Courier",Font.PLAIN, 12));
			_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			//_table.setRowHeight(30);
			//_table.setPreferredScrollableViewportSize(new Dimension(500, 500));
			// custom cell renderer for Time column
			DateRenderer renderer = new DateRenderer();
			TableColumn column = _table.getColumnModel().getColumn(TIME_COLUMN);
			//_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			column.setPreferredWidth(200);
			column.setCellRenderer(renderer);
			// custom cell renderer for Actual & Forecast columns
			setDecimalRenderer(ACTUAL_COLUMN);
			setDecimalRenderer(FORECAST_COLUMN);
			setDecimalRenderer(DIFF_COLUMN);
			_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		void setDecimalRenderer(int column)
		{
			setDecimalRenderer(column, null);
		}
	   void setDecimalRenderer(int column, String format)
	   {
			DecimalRenderer decRenderer = null;
			if (format != null)
				decRenderer = new DecimalRenderer(format);
			else
				decRenderer = new DecimalRenderer();

			TableColumn col = _table.getColumnModel().getColumn(column);
			col.setCellRenderer(decRenderer);

	   }
	   public JTable getTable()
	   {
		   return _table;
	   }
	   public void sortTable()
	   {
		   JTable table = getTable();
		   if (table.getRowSorter() != null) {
			   sorter.sort();
		   }
	   }

}
