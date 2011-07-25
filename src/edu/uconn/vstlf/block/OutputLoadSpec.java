package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.prediction.DataFeed;

public class OutputLoadSpec extends BlockSpec {

	private int lvl_, nInputHours_;
	
	public OutputLoadSpec(int lvl, int nInputHours)
	{
		lvl_ = lvl;
		nInputHours_ = nInputHours;
	}
	
	@Override
	public
	InputBlock getInputBlock(DataFeed feed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public
	OutputBlock getOutputBlock(DataFeed feed) throws Exception {
		// TODO Auto-generated method stub
		int nLvls = feed.getNDecompLvls();
		Series maxInput = feed.getDecomposedLoads(0, nInputHours_)[nLvls];
		return new OutputLoadBlock(maxInput, lvl_, nLvls);
	}

	@Override
	public
	UpdateBlock getUpdateBlock(DataFeed feed) {
		// TODO Auto-generated method stub
		return null;
	}
}
