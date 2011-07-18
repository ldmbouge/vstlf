package edu.uconn.vstlf.prediction;

import java.util.Vector;

import edu.uconn.vstlf.block.BlockSpec;
import edu.uconn.vstlf.block.InputBlock;
import edu.uconn.vstlf.block.OutputBlock;
import edu.uconn.vstlf.block.UpdateBlock;

public class ANNSpec {

	private Vector<BlockSpec> inputSpecs_;
	private BlockSpec outputSpecs_;
	private BlockSpec updateSpecs_;
	private int[] lyrSz_;
	
	public int[] getLayerSize() { return lyrSz_; }
	
	public ANNSpec(Vector<BlockSpec> inputSpecs,
			BlockSpec outputSpecs, BlockSpec updateSpecs,
			int[] lyrSz)
	{
		inputSpecs_ = inputSpecs;
		outputSpecs_ = outputSpecs;
		updateSpecs_ = updateSpecs;
		lyrSz_ = lyrSz;
	}
	
	public Vector<InputBlock> getInputBlocks(DataFeed feed) throws Exception
	{
		Vector<InputBlock> blks = new Vector<InputBlock>();
		for (BlockSpec inSpec : inputSpecs_)
			blks.add(inSpec.getInputBlock(feed));
		return blks;
	}
	
	public OutputBlock getOutputBlock(DataFeed feed) throws Exception
	{
		return outputSpecs_.getOutputBlock(feed);
	}
	
	public UpdateBlock getUpdateBlock(DataFeed feed) throws Exception
	{
		return updateSpecs_.getUpdateBlock(feed);
	}
}
