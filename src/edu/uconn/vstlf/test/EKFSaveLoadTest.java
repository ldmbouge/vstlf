package edu.uconn.vstlf.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import edu.uconn.vstlf.matrix.Matrix;
import edu.uconn.vstlf.neuro.ekf.EKFANN;

public class EKFSaveLoadTest {

	@Test public void saveLoadTest() throws Exception
	{
		String testBank = "bank_test.ann";
		int testId = 1;
		EKFANN ann = EKFANN.load("bank0.ann", testId);
		ann.save(testBank, testId);
		EKFANN cmpAnn = EKFANN.load(testBank, testId);
		double[] wSave = ann.getWeights();
		double[] wLoad = cmpAnn.getWeights();
		assertTrue(wSave.length == wLoad.length);
		for (int i = 0; i < wSave.length; ++i)
			assertTrue(wSave[i] == wLoad[i]);
		
		Matrix PSave = ann.getP();
		Matrix PLoad = cmpAnn.getP();
		assertTrue(PSave.getRow() == PLoad.getRow());
		assertTrue(PSave.getCol() == PLoad.getCol());
		for (int i = 0; i < PSave.getRow(); ++i)
			for (int j = 0; j < PSave.getCol(); ++j)
				assertTrue(PSave.getVal(i, j) == PLoad.getVal(i, j));


		File f = new File(testBank);
		f.delete();
	}
}
