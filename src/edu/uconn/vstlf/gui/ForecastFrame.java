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
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

class ForecastFrame extends JPanel implements IForecastFrame {
   final int _xOffset = 675, _yOffset = 5;
   JTable _table = null;
   TableRowSorter<TableModel> sorter = null;

   public ForecastFrame(DefaultTableModel model, String titleBar) {
	/*super(titleBar,
		  true, //resizable
		  false, //closable
		  false, //maximizable
		  true);//iconifiable
		  */
	setFocusable(true);
	_table = new JTable(model);
	setLayout(new BorderLayout());
    sorter = new TableRowSorter<TableModel>(model);
    _table.setRowSorter(sorter);
	setupJTable();

	//Create the scroll pane and add the table to it.
	JScrollPane scrollPane = new JScrollPane(_table);
	//Add the scroll pane to this window.
	add(BorderLayout.CENTER,scrollPane);
	// Set the frame characteristics
	//setSize(400, 430);
	//Set the window's location.
	//setLocation(_xOffset, _yOffset);
   }
	void setupJTable()
	{
		_table.setFont(new Font("Courier",  Font.PLAIN, 12));
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//_table.setRowHeight(30);
		//_table.setPreferredScrollableViewportSize(new Dimension(300, 500));

		// custom cell renderer for Time column
		DateRenderer renderer = new DateRenderer();
		TableColumn column = _table.getColumnModel().getColumn(TIME_COLUMN);
		//_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		column.setPreferredWidth(200);
		column.setCellRenderer(renderer);
		//
		// custom cell renderer for Forecast column
		setDecimalRenderer(FORECAST_COLUMN);
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