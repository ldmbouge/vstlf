package edu.uconn.vstlf.block;

import edu.uconn.vstlf.prediction.DataFeed;

public class SunsetIndexSpec extends BlockSpec {

	@Override
	public
	InputBlock getInputBlock(DataFeed feed) throws Exception {
		// TODO Auto-generated method stub
		return new SunsetIndexBlock(feed.getCurTime(), feed.getCalendar());
	}

	@Override
	public
	OutputBlock getOutputBlock(DataFeed feed) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public
	UpdateBlock getUpdateBlock(DataFeed feed) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
