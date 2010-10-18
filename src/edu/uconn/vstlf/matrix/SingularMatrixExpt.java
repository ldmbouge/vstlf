package edu.uconn.vstlf.matrix;

public class SingularMatrixExpt extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2605764272189234068L;
	
	public SingularMatrixExpt(Matrix mtrx, String opDesc)
	{
		super("matrix of size " + mtrx.getRow() + "*" + mtrx.getCol() + " is a singular matrix(" + opDesc +")");
	}
}
