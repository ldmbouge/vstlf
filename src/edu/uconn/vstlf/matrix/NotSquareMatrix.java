package edu.uconn.vstlf.matrix;

public class NotSquareMatrix extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1624980344339396271L;
	
	NotSquareMatrix(Matrix mtrx, String opDesc)
	{
		super("Matrix of size " + mtrx.getRow() + "*" + mtrx.getCol() + " is not a square matrix(" + opDesc + ")");
	}
}
