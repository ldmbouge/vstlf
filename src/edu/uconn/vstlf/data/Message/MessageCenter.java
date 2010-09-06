package edu.uconn.vstlf.data.Message;

import edu.uconn.vstlf.realtime.PCBuffer;

public class MessageCenter implements Runnable {	
	private Thread thrd_; 
	
	public void init()
	{
		thrd_ = new Thread(this);
		thrd_.start();
	}
	
	private static MessageCenter instance_ = new MessageCenter();
	public static MessageCenter getInstance() { return instance_; }

	public void put(VSTLFMessage msg)
	{
		buf_.produce(msg);
	}
	
	private MessageCenter()
	{
		buf_ = new PCBuffer<VSTLFMessage>(1024);
	}
	
	private PCBuffer<VSTLFMessage> buf_;
	private VSTLFMsgHandler handlerChain_;
	
	public void setHandler(VSTLFMsgHandler handler) { handlerChain_ = handler; }
	public void dispatch(VSTLFMessage msg) throws VSTLFMsgException
	{
		handlerChain_.handle(msg);
	}
	
	@Override
	/* dispatch the messages */
	public void run() {
		try {
			while (true) {
				VSTLFMessage msg = buf_.consume();
				if (msg.getType() == VSTLFMessage.Type.EOF)
					break;
		
					dispatch(msg);
			}
		} catch (VSTLFMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}