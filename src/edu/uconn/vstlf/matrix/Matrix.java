package edu.uconn.vstlf.matrix;

import java.util.Vector;

public class Matrix {
	private double[][] mtrx_;
	
	public Matrix(int row, int column)
	{
		mtrx_ = new double[row][column];
	}
	
	public String toString()
	{
		String s = "{";
		for (int r = 0; r < getRow(); ++r) {
			s += "[";
			for (int c = 0; c < getCol(); ++c) {
				s = s + getVal(r, c);
				if (c != getCol() - 1)
					s += ", ";
			}
			s += "]";
			if (r != getRow() - 1)
				s += ", ";
		}
		s += "}";
		return s;
	}
	
	public Matrix(double [][] vals) { mtrx_ = vals; }
	
	public int getRow() { return mtrx_.length; }
	public int getCol() { return mtrx_[0].length; }
	
	public Vector<Double> getRowVec(int r) 
	{ 
		Vector<Double> rVals = new Vector<Double>(getCol());
		for (int c = 0; c < getCol(); ++c)
			rVals.set(c, getVal(r, c));
		return rVals;
	}
	
	public Vector<Double> getColVec(int c) 
	{ 
		Vector<Double> cVals = new Vector<Double>(getRow());
		for (int r = 0; r < getRow(); ++r)
			cVals.set(r, getVal(r, c));
		return cVals;
	}
	
	public double getVal(int r, int c)
	{
		return mtrx_[r][c];
	}
	
	public void setVal(int r, int c, double v)
	{
		mtrx_[r][c] = v;
	}
	
	public static void add(Matrix m, Matrix other) throws IncompatibleMatrixExpt
	{
		if (m.getRow() != other.getRow() || m.getCol() != other.getCol())
			throw new IncompatibleMatrixExpt(m, other, "add matrix");
		for (int r = 0; r < m.getRow(); ++r)
			for (int c = 0; c < m.getCol(); ++c)
				m.setVal(r, c, m.getVal(r, c) + other.getVal(r, c));
	}
	
	public static void subtract(Matrix m, Matrix other) throws IncompatibleMatrixExpt
	{
		if (m.getRow() != other.getRow() || m.getCol() != other.getCol())
			throw new IncompatibleMatrixExpt(m, other, "subtract matrix");
		for (int r = 0; r < m.getRow(); ++r)
			for (int c = 0; c < m.getCol(); ++c)
				m.setVal(r, c, m.getVal(r, c) - other.getVal(r, c));
	}
	
	public static void multiply(Matrix m1, Matrix m2, Matrix result) throws IncompatibleMatrixExpt
	{
		if (m1.getCol() != m2.getRow())
			throw new IncompatibleMatrixExpt(m1, m2, "multiply matrices");
		if (m1.getRow() != result.getRow())
			throw new IncompatibleMatrixExpt(m1, result, "the row size of result matrix is not the same as multiplier's");
		if (m1.getRow() != result.getCol())
			throw new IncompatibleMatrixExpt(m2, result, "the column size of result matrix is not the same as multiplicand's");
		
		for (int r = 0; r < m1.getRow(); ++r)
			for (int c = 0; c < m2.getCol(); ++c) {
				double v = 0.0;
				for (int k = 0; k < m1.getCol(); ++k)
					v += m1.getVal(r, k)*m2.getVal(k, c);
				result.setVal(r, c, v);
			}
	}
	
	public static void multiply(double coeff, Matrix m)
	{
		for (int r = 0; r < m.getRow(); ++r)
			for (int c = 0; c < m.getCol(); ++c)
				m.setVal(r, c, coeff*m.getVal(r, c));
	}
		
	public static void copy(Matrix mtrx, Matrix targ) throws IncompatibleMatrixExpt
	{
		if (mtrx.getRow() != targ.getRow() || mtrx.getCol() != targ.getCol())
			throw new IncompatibleMatrixExpt(mtrx, targ, "copy matrix");
		for (int r = 0; r < mtrx.getRow(); ++r)
			for (int c = 0; c < mtrx.getCol(); ++c)
				targ.setVal(r, c, mtrx.getVal(r, c));
	}
	
	public static void transpose(Matrix mtrx, Matrix targ) throws IncompatibleMatrixExpt
	{
		if (mtrx.getRow() != targ.getCol() || mtrx.getCol() != targ.getRow())
			throw new IncompatibleMatrixExpt(mtrx, targ, "transpose matrix");
		for (int r = 0; r < targ.getRow(); ++r)
			for (int c = 0; c < targ.getCol(); ++c)
				targ.setVal(r, c, mtrx.getVal(c, r));	
	}
	
	public static Vector<Integer> LUPDecompose(Matrix mtrx) throws NotSquareMatrix, SingularMatrixExpt
	{
		if (mtrx.getRow() != mtrx.getCol())
			throw new NotSquareMatrix(mtrx, "LUP Decomposition");
		
		int n = mtrx.getRow();
		Vector<Integer> permVec = new Vector<Integer>(n);
		// Initialize the permutation vector
		for (int i = 0; i < n; ++i)
			permVec.add(i);
		
		// LUP decompose
		for (int i = 0; i < n; ++i) {
			// select the row with maximum head value
			int maxR = i; 
			double maxV = Math.abs(mtrx.getVal(maxR, i));
			for (int k = i+1; k < n; ++k) {
				if (Math.abs(mtrx.getVal(k, i)) > maxV) {
					maxR = k;
					maxV = Math.abs(mtrx.getVal(k, i));
				}	
			}
			if (maxV == 0.0)
				throw new SingularMatrixExpt(mtrx, "LUP decomposition");
			
			if (maxR != i) {
				// Adjust the permutation vector
				int tempi = permVec.get(i);
				permVec.set(i, permVec.get(maxR));
				permVec.set(maxR, tempi);
				
				// Exchange the i'th row and the maxR'th row of this matrix
				for (int k = 0; k < n; ++k) {
					double tempv = mtrx.getVal(maxR, k);
					mtrx.setVal(maxR, k, mtrx.getVal(i, k));
					mtrx.setVal(i, k, tempv);
				}
			}
			
			// LU decompose the sub matrix (Shur complement)
			double headV = mtrx.getVal(i, i);
			for (int k = i+1; k < n; ++k) {
				double coeff = mtrx.getVal(k, i)/headV;
				mtrx.setVal(k, i, coeff);
				for (int u = i+1; u < n; ++u)
					mtrx.setVal(k, u, mtrx.getVal(k, u) - coeff*mtrx.getVal(i, u));
			}	
			
			//System.out.println(i + "'th iteration: " + mtrx);
		}
		return permVec;
	}
	
	public static void LUSolve(Matrix LUMtrx, Vector<Double> b) throws NotSquareMatrix
	{
		if (LUMtrx.getRow() != LUMtrx.getCol())
			throw new NotSquareMatrix(LUMtrx, "LUSolve");
		
		int n = LUMtrx.getRow();
		// Forward substitution (using L)
		for (int i = 0; i < n; ++i) {
			double delSum = 0.0;
			for (int j = 0; j < i; ++j)
				delSum += LUMtrx.getVal(i, j)*b.get(j);
			b.set(i, b.get(i)-delSum);
		}
		
		// Back substitution (using U)
		for (int i = n-1; i >= 0; --i) {
			double delSum = 0.0;
			for (int j = i+1; j < n; ++j)
				delSum += LUMtrx.getVal(i, j)*b.get(j);
			b.set(i, (b.get(i) - delSum)/LUMtrx.getVal(i, i));
		}
	}
	
	public static void inverse(Matrix mtrx, Matrix result) throws NotSquareMatrix, IncompatibleMatrixExpt, SingularMatrixExpt
	{
		if (mtrx.getRow()  != mtrx.getCol())
			throw new NotSquareMatrix(mtrx, "inverse");
		if (result.getRow()  != result.getCol())
			throw new NotSquareMatrix(result, "inverse");
		if (mtrx.getRow() != result.getRow())
			throw new IncompatibleMatrixExpt(mtrx, result, "inverse");
		Vector<Integer> permVec = LUPDecompose(mtrx);
		
		int n = mtrx.getRow();
		for (int i = 0; i < n; ++i) {
			// solve the equation for the permVec[i]'th column of the result matrix
			Vector<Double> b = new Vector<Double>(n);
			for (int j=0; j < n; ++j)
				b.add(j==i ? 1.0: 0.0);
			LUSolve(mtrx, b);
			// put the result into the permVec[i]'th column of the result matrix
			int col = permVec.get(i);
			for (int r = 0; r < n; ++r)
				result.setVal(r, col, b.get(r));
		}
	}
	
	public static boolean equal(Matrix m1, Matrix m2, double e) throws IncompatibleMatrixExpt
	{
		if (m1.getRow() != m2.getRow() || m1.getCol() != m2.getCol())
			throw new IncompatibleMatrixExpt(m1, m2, "equal e=" + e);
		for (int r = 0; r < m1.getRow(); ++r)
			for (int c = 0; c < m1.getCol(); ++c)
				if (Math.abs(m1.getVal(r, c) - m2.getVal(r, c)) > e)
					return false;
		
		return true;
	}
}


