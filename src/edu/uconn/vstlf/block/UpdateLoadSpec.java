package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.prediction.DataFeed;

public class UpdateLoadSpec extends BlockSpec {

	private int lvl_, nInputHours_, nOutputHours_;
	
	public UpdateLoadSpec(int lvl, int nInputHours, int nOutputHours) 
	{
		lvl_ = lvl;
		nInputHours_ = nInputHours;
		nOutputHours_ = nOutputHours;
	}
	
	@Override
	public
	InputBlock getInputBlock(DataFeed feed) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
		int nLvls = feed.getNDecompLvls();
		Series refOut = feed.getDecomposedLoads(0, nOutputHours_)[lvl_];
		Series lastMaxInput = feed.getDecomposedLoads(-nOutputHours_, nInputHours_)[nLvls];
		return new UpdateLoadBlock(refOut, lastMaxInput, lvl_, nLvls);
	}

	public int getInputHours()
	{
		return nInputHours_;
	}
	
	public int getOutputHours()
	{
		return nOutputHours_;
	}
}
