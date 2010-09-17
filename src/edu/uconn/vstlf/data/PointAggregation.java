package edu.uconn.vstlf.data;

import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.data.message.VSTLFMessage;
import edu.uconn.vstlf.realtime.VSTLFObservationPoint;

public class PointAggregation {
	public PointAggregation(
			LinkedList<VSTLFObservationPoint> inpts, 
			LinkedList<VSTLFObservationPoint> outpts,
			LinkedList<Integer> nbObjs,
			Date at, int inc)
	{
		inpts_ = inpts;
		outpts_ = outpts;
		nbObjs_ = nbObjs;
		at_ = at;
		inc_ = inc;
	}
	
	public void aggregate()
	{
		if (inpts_.isEmpty()) return;
		
		// The points before 'at'(including the one at 'at') are discarded
		while ( !inpts_.getFirst().getStamp().after(at_) ) {
			MessageCenter.getInstance().put(new LogMessage(Level.WARNING, "PointAggregation", "aggregate", 
					"Load at " + inpts_.getFirst().getStamp() + " is out of order"));
			inpts_.removeFirst();
		}
		
		Calendar cal = Items.makeCalendar();
		Date nextAt = cal.addSecondsTo(at_, inc_);
		while (!inpts_.isEmpty() && !nextAt.after(inpts_.getLast().getStamp())) {
			// aggregate the nodes between 'at' and 'nextAt' (not including node at 'at')
			int n = 0;
			double loadSum = 0.0;
			while ( !inpts_.isEmpty() && !inpts_.getFirst().getStamp().after(nextAt) ) {
				loadSum += inpts_.getFirst().getValue();
				++n;
				inpts_.removeFirst();
			}
			if (n > 0)
				outpts_.add(new VSTLFObservationPoint(VSTLFMessage.Type.RTPoint, nextAt, (loadSum/n)));
			else
				outpts_.add(new VSTLFObservationPoint(VSTLFMessage.Type.RTPoint, nextAt, 0.0));
			nbObjs_.add(n);
			at_ = nextAt;
			nextAt = cal.addSecondsTo(at_, inc_);
		}
	}
	
	private LinkedList<VSTLFObservationPoint> inpts_;
	private LinkedList<VSTLFObservationPoint> outpts_;
	private LinkedList<Integer> nbObjs_;
	private Date at_;
	private int inc_;
}
