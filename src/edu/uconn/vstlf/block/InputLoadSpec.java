package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.prediction.DataFeed;


public class InputLoadSpec extends BlockSpec {
	private int lvl_;
	
	public InputLoadSpec(int lvl)
	{
		lvl_ = lvl;
	}

	@Override
	public
	InputBlock getInputBlock(DataFeed feed) throws Exception {
		// TODO Auto-generated method stub
		Series input = feed.getDecomposedLoads(0)[lvl_];
		return new InputLoadBlock(input, lvl_, feed.getNDecompLvls());
	}

	@Override
	public
	OutputBlock getOutputBlock(DataFeed feed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public
	UpdateBlock getUpdateBlock(DataFeed feed) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
