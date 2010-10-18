package edu.uconn.vstlf.matrix;

public class IncompatibleMatrixExpt extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9210585621269970398L;
	
	public IncompatibleMatrixExpt(Matrix m1, Matrix m2, String opDesc)
	{
		super("Matrix of size " + m1.getRow() + "*" + m1.getCol() + " is not compatible with matrix of size "
				+ m2.getRow() + "*" + m2.getCol() + "(" + opDesc + ")");
	}
}
