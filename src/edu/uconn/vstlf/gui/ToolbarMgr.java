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

import javax.swing.JToolBar;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.BorderLayout;

import java.util.Date;
import java.text.SimpleDateFormat;

public class ToolbarMgr
{
	// upper tool bar components
	JTextField _currentTimeTextField = new JTextField (new Date().toString());
	JTextField _lastObservationTimeTextField = new JTextField (IToolbarMgr.UNKNOWN_DATE);
	JTextField _lastObservationValueTextField = new JTextField (IToolbarMgr.UNKNOWN_VALUE);
    SimpleDateFormat _formatter = new SimpleDateFormat(IVstlfMain.DATE_FORMAT);


	public ToolbarMgr()
	{

	}
   public void createToolbars()
   {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JLabel("Pulse Time  "));
		setupTextField(_currentTimeTextField);
		toolBar.add(_currentTimeTextField);
		toolBar.add(new JLabel("  ObservationTime  "));
		setupTextField(_lastObservationTimeTextField);
		toolBar.add(_lastObservationTimeTextField);

		setupTextField(_lastObservationValueTextField);
		toolBar.add(new JLabel("  Value  "));
		setupTextField(_lastObservationValueTextField);
		toolBar.add(_lastObservationValueTextField);


		IsoVstlfGui.getIsoVstlfGui().getContentPane().add(toolBar, BorderLayout.NORTH);
	}

	public void update(Date pulseTime, Date observationTime, double value)
	{
		// update the toolbar fields
		_currentTimeTextField.setText(formatDate(pulseTime));
		_lastObservationTimeTextField.setText(formatDate(observationTime));
		_lastObservationValueTextField.setText(Double.toString(value));
	}

	void setupTextField(JTextComponent in)
	{
		in.setEditable(false);
		//in.setFont(new Font("Ariel",  Font.BOLD, 16));
		in.setFont(new Font("Courier",Font.PLAIN, 12));
	}
	String formatDate(Date in)
	{
		String formattedDate = IToolbarMgr.UNKNOWN_DATE;
		if (in == null) {
			return formattedDate;
		}
		try {
			formattedDate = _formatter.format(in);
		}
		catch (Exception e) {
			e.printStackTrace();

		}
		return formattedDate;

	}
}