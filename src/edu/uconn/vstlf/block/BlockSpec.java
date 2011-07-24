package edu.uconn.vstlf.block;


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uconn.vstlf.prediction.DataFeed;

public abstract class BlockSpec {	
	public static BlockSpec createFromElement(Element e, Element net) throws Exception
	{
		BlockType typ = BlockType.valueOf(e.getAttribute("type"));
		switch (typ)
		{
		case InputLoad: {
			int lvl = Integer.parseInt(e.getAttribute("lvl"));
			int hours = Integer.parseInt(e.getAttribute("hours"));
			return new InputLoadSpec(lvl, hours);
		}
		case OutputLoad: {
			Element inBlock = findInputLoadBlock(net);
			int lvl = Integer.parseInt(e.getAttribute("lvl"));
			int hours = Integer.parseInt(inBlock.getAttribute("hours"));
			return new OutputLoadSpec(lvl, hours);
		}
		case UpdateLoad: {
			Element inBlock = findInputLoadBlock(net);
			Element outBlock = findOutputLoadBlock(net);
			int lvl = Integer.parseInt(e.getAttribute("lvl"));
			int inHours = Integer.parseInt(inBlock.getAttribute("hours"));
			int outHours = Integer.parseInt(outBlock.getAttribute("hours"));
			return new UpdateLoadSpec(lvl, inHours, outHours);
		}
		case SunsetIndex: {
			return new SunsetIndexSpec();
		}
		case WeekIndex: {
			return new WeekIndexSpec();
		}
		case MonthIndex: {
			return new MonthIndexSpec();
		}
		case HourIndex: {
			return new HourIndexSpec();
		}
		
		default:
			throw new Exception("Cannot create the block specification for type: " + typ);
		}
	}
	
	public abstract InputBlock getInputBlock(DataFeed feed) throws Exception;
	public abstract OutputBlock getOutputBlock(DataFeed feed) throws Exception;
	public abstract UpdateBlock getUpdateBlock(DataFeed feed) throws Exception;
	
	private static Element findInputLoadBlock(Element net)
	{
		NodeList list = net.getElementsByTagName("InputBlock");
		for (int i = 0; i < list.getLength(); ++i) {
			Element e = (Element)list.item(i);
			BlockType typ = BlockType.valueOf(e.getAttribute("type"));
			if (typ == BlockType.InputLoad)
				return e;
		}
		return null;
	}
	
	private static Element findOutputLoadBlock(Element net)
	{
		NodeList list = net.getElementsByTagName("OutputBlock");
		for (int i = 0; i < list.getLength(); ++i) {
			Element e = (Element)list.item(i);
			BlockType typ = BlockType.valueOf(e.getAttribute("type"));
			if (typ == BlockType.OutputLoad)
				return e;
		}
		return null;
	}
}
