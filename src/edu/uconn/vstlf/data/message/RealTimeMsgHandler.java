package edu.uconn.vstlf.data.message;

import edu.uconn.vstlf.realtime.VSTLFNotificationCenter;
import edu.uconn.vstlf.realtime.VSTLFRealTimeMessage;

public class RealTimeMsgHandler extends VSTLFMsgHandler {

	public RealTimeMsgHandler(VSTLFNotificationCenter center,
			VSTLFMsgHandler successor)
	{
		center_ = center;
		successor_ = successor;
	}
	
	@Override
	public void handle(VSTLFMessage msg) throws VSTLFMsgException {
		if (VSTLFMessage.toGroup(msg.getType()) == VSTLFMessage.Group.RealTime) {
			// Deal with the real time message
			VSTLFRealTimeMessage relmsg = (VSTLFRealTimeMessage)msg;
			relmsg.visit(center_);
		}
		else
			successor_.handle(msg);
	}
	
	private VSTLFNotificationCenter center_;
}
