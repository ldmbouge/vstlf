package edu.uconn.vstlf.data.message;

public class DummyMsgHandler extends VSTLFMsgHandler {

	@Override
	public void handle(VSTLFMessage msg) throws VSTLFMsgException {
		// TODO Auto-generated method stub
		 System.err.println(msg.getClass().getName() + " not handled");
	}

}
