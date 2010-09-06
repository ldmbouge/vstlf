package edu.uconn.vstlf.data.Message;

public class VSTLFMsgException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2529508999630422678L;

	VSTLFMsgException(VSTLFMessage msg, String info)
	{
		super(msg.getClass().getName()+info);
	}
}
