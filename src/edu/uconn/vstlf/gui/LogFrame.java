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

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class LogFrame extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7430016531702059484L;
	//static int openFrameCount = 0;
    final static int xOffset = 660, yOffset = 445;
	JTextArea textArea;
	public LogFrame() {
    	setLayout(new GridLayout());
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Courier",  Font.PLAIN, 11));
		textArea.setLineWrap(false);
		textArea.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setAutoscrolls(true);
		areaScrollPane.setPreferredSize(new Dimension(600, 200));


		add(areaScrollPane);
		//c.add(new JButton(), BorderLayout.CENTER);

        //...Then set the window size or call pack...
        setSize(500,150);

        //Set the window's location.
        setLocation(xOffset, yOffset);
    }
    synchronized public void addMessage(String in)
    {
		if (textArea != null) {
			textArea.append(in);
			textArea.setCaretPosition(textArea.getText().length());
		}
	}
    
    synchronized public void dump(String fname){
    	System.out.println("Dumping to "+fname);
    	FileWriter fw = null;
    	try{
    		fw = new FileWriter(new File(fname));
    		fw.write(textArea.getText());
    		fw.close();
    	}
    	catch(Exception e){
    		addMessage("DUMP OF LOG TO FILE '"+fname+"' FAILED.");
    		return;
    	}
    	textArea.setText("");
    }
    
    public String toString(){
    	return textArea.getText();
    }
}
