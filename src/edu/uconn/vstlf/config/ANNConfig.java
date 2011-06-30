package edu.uconn.vstlf.config;

import java.io.File;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uconn.vstlf.block.BlockSpec;
import edu.uconn.vstlf.prediction.ANNSpec;

public class ANNConfig {
	static private ANNConfig inst_;
	static public ANNConfig getInstance() throws Exception
	{
		if (inst_ == null) inst_ = new ANNConfig("config.xml");
		return inst_;
	}
	
	private ANNSpec[] annSpecs_;
	public ANNSpec[] getANNSpecs() { return annSpecs_; }
	
	private ANNConfig(String configFile) throws Exception
	{
		 File fXmlFile = new File(configFile);
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		 Document doc = dBuilder.parse(fXmlFile);		  
		 Element root = (Element)doc.getDocumentElement();

		 NodeList dcElems = root.getElementsByTagName("Network");
		 int nANNs = DaubConfig.getInstance().getDaubSpec().getNumLevels();
		 
		 if (dcElems.getLength() != nANNs)
			 throw new Exception("Number of networks is not compatible to the daub decomposition. Please check the config file");
		 
		 annSpecs_ = new ANNSpec[nANNs];
		 for (int i = 0; i < nANNs; ++i) {
			 Element elem = (Element)dcElems.item(i);
			 int lvl = Integer.parseInt(elem.getAttribute("lvl"));
			 annSpecs_[lvl] = createANNSpec(elem);
		 }
	}
	
	private ANNSpec createANNSpec(Element e) throws Exception
	{
		// get input blocks
		Vector<BlockSpec> inputSpecs = new Vector<BlockSpec>();
		NodeList inputElems = e.getElementsByTagName("InputBlock");
		for (int i = 0; i < inputElems.getLength(); ++i) {
			Element inputE = (Element)inputElems.item(i);
			inputSpecs.add(BlockSpec.createFromElement(inputE));
		}
		
		// get output block
		Element outputElem = (Element)e.getElementsByTagName("OutputBlock").item(0);
		BlockSpec outputSpec = BlockSpec.createFromElement(outputElem);
		
		// get the update block
		Element updateElem = (Element)e.getElementsByTagName("UpdateBlock").item(0);
		BlockSpec updateSpec = BlockSpec.createFromElement(updateElem);
		
		return new ANNSpec(inputSpecs, outputSpec, updateSpec);
	}
}
