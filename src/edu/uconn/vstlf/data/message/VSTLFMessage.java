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

package edu.uconn.vstlf.data.message;


public class VSTLFMessage {
	public static enum Group { RealTime, PreProcessing, Training, Misc, Unknown };
	public static enum Type { RTFiveMin, RTFourSec, RTMissing4s, RTMissing5m, 
		RTPred, RTRefine4s, RTRefine5m, RT4sPoint, RT5mPoint, RTPoint,
		RTException, Log, EOF, Unknown
	}
	public static Group toGroup(Type type)
	{
		switch (type) 
		{
		case RTFiveMin:
		case RTFourSec:
		case RTMissing4s:
		case RTMissing5m: 
		case RTPred:
		case RTRefine4s: 
		case RTRefine5m:
		case RT4sPoint:
		case RT5mPoint:
		case RTException:
			return Group.RealTime;
			
		case Log:
		case EOF:
			return Group.Misc;
		}
		return Group.Unknown;
	}
	
	public VSTLFMessage(Type type)
	{
		type_ = type;
	}
	
	public Type getType() { return type_; }
	
	private final Type type_;
}
