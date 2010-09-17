package edu.uconn.vstlf.realtime;

import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;

import edu.uconn.vstlf.data.PointAggregation;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;


public class Integrate4sLoad implements Runnable{
private Thread _feedThread;
	
	private PCBuffer<VSTLFObservationPoint> _input;
	private PCBuffer<VSTLF4SPoint> _output;
	private int _inc;
	private Date _at;
	
	public Integrate4sLoad(PCBuffer<VSTLFObservationPoint> input, PCBuffer<VSTLF4SPoint> output,
			Date at, int rate) {
		_input = input;
		_output = output;
		_inc = rate;
		_at = at;
	}
	
	public void init()
	{
		_feedThread = new Thread(this);
		_feedThread.start();
	}
	
	public void run()
	{
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, "Integrate4sLoad", "run", "Thread for integrating 4s loads starts running"));

		LinkedList<VSTLFObservationPoint> inputAgg = new LinkedList<VSTLFObservationPoint>();
		LinkedList<VSTLFObservationPoint> outputAgg = new LinkedList<VSTLFObservationPoint>();
		LinkedList<Integer> nbObjs = new LinkedList<Integer>();
		PointAggregation agg = new PointAggregation(inputAgg, outputAgg, nbObjs, _at, _inc);
		
		VSTLFObservationPoint point;
		while ( (point = _input.consume()).getType() != VSTLFMessage.Type.EOF ) {
			if (!point.isValid())  {
				MessageCenter.getInstance().put(new LogMessage(Level.SEVERE, "IntegrateFourSecLoad", "run", "Invalid 4s point at " + point.getStamp()));
				continue;
			}
			
			if (point.getValue()<=0) {
				MessageCenter.getInstance().put(new LogMessage(Level.SEVERE, "IntegrateFourSecLoad", "run", "Negative load " + point.getValue() + " at " + point.getStamp()));
				continue;
			}
			
			inputAgg.add(point);
			agg.aggregate();
			while (!outputAgg.isEmpty()) {
				VSTLFObservationPoint aggPoint = outputAgg.getFirst();
				int nbObj = nbObjs.getFirst();
				if (nbObj == 0) {
					_output.produce(new VSTLF4SPoint(aggPoint.getStamp(), 0.0));
					MessageCenter.getInstance().put(new MessageMissing4s(aggPoint.getStamp()));
				}
				else {
					_output.produce(new VSTLF4SPoint(aggPoint.getStamp(), aggPoint.getValue()));
					MessageCenter.getInstance().put(new FourSecMessage(aggPoint.getStamp(), aggPoint.getValue(), nbObj));
				}
				outputAgg.removeFirst();
			}
			
		} // while
		_output.produce(new VSTLF4SPoint(VSTLFMessage.Type.EOF, _at, 0.0));
		MessageCenter.getInstance().put(new LogMessage(Level.INFO, "Integrate4sLoad", "run", "Thread for integrating 4s loads stops"));
	}
	
	public void join() throws InterruptedException
	{
		_feedThread.join();
	}


}
