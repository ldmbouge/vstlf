package edu.uconn.vstlf.data.message;

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
	
	private void dispatch(VSTLFMessage msg) throws VSTLFMsgException
	{
		handlerChain_.handle(msg);
	}
	
	@Override
	/* dispatch the messages */
	public void run() {
		try {
			VSTLFMessage msg;
			while ( (msg = buf_.consume()).getType() != VSTLFMessage.Type.StopMessageCenter ) {
				dispatch(msg);
			}
		} catch (VSTLFMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void join() throws InterruptedException
	{
		thrd_.join();
	}
}