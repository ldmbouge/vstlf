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

public interface IDistributionFrame
{

   final int MINUTES_COLUMN = 0;
   final int MAPE_COLUMN  	= 1;
   final int MAE_COLUMN 	= 2;
   final int DEV_COLUMN 	= 3;
   final int MAXOVER_COLUMN = 4;
   final int MAXUNDR_COLUMN = 5;

	final String[] DISTRIBUTION_COLUMN_NAMES = {
		"Minutes Ahead",
		"MAPE",
		"MAE",
		"StDev",
		"Max Ovr Err",
		"Max Und Err"
		};

	final String DECIMAL_FORMAT_SINGLE = "0";
	final String DECIMAL_FORMAT_DOUBLE = "0.00";


}