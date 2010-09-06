package edu.uconn.vstlf.data.Message;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class VSTLFMsgLogger extends VSTLFMsgHandler {
	
	public VSTLFMsgLogger(String filename) throws SecurityException, IOException
	{
		logger_ = Logger.getLogger("VSTLFLogger");
		Handler hf = new FileHandler(filename);
		logger_.addHandler(hf);
	}
	
	@Override
	public void handle(VSTLFMessage msg) throws VSTLFMsgException {
		// TODO Auto-generated method stub
		if (msg.getType() == VSTLFMessage.Type.Log) {
			LogMessage logmsg= (LogMessage)msg;
			logger_.logp(logmsg.getLevel(), logmsg.getSrcClass(), 
					logmsg.getSrcMethod(), logmsg.getMsg());
		}
		else
			successor_.handle(msg);
	}

	private Logger logger_;
}
