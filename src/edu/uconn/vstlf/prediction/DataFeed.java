package edu.uconn.vstlf.prediction;

import java.util.Vector;

public class DataFeed {
	private Vector<InputBlock> inputBlks_;
	
	public DataFeed(InputBlock[] originInputBlks) throws Exception
	{
		// Create input blocks, including the derived input
		// blocks from the original blocks
		for (int i = 0; i < originInputBlks.length; ++i) {
			InputBlock blk = originInputBlks[i];
			inputBlks_.add(blk);
			Vector<InputBlock> derivedInputs = blk.deriveInputBlocks();
			for (int j = 0; j < derivedInputs.size(); ++j)
				inputBlks_.add(derivedInputs.get(j));
		}
	}
	
	
}
