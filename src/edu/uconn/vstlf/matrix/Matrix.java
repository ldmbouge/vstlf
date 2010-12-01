package edu.uconn.vstlf.matrix;

public class Matrix {
	private double[][] mtrx_;
	private boolean isRMjr_;
	
	public Matrix(int row, int column, boolean isRowMajor)
	{
		mtrx_ = new double[row][column];
		isRMjr_ = isRowMajor;
	}
	
	public Matrix(int row, int column)
	{
		this(row, column, true);
	}
	
	/*
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
	}*/
	
	public Matrix(double [][] vals, boolean isRowMajor) 
	{ isRMjr_ = isRowMajor; mtrx_ = vals; }
	
	public Matrix(double[][] vals)
	{ this(vals, true); }
	
	public int getRow() { return (isRMjr_ ? mtrx_.length : mtrx_[0].length); }
	public int getCol() { return (isRMjr_ ? mtrx_[0].length : mtrx_.length); }
	
	public double[] getRowVec(int r) 
	{ 
		double[] rVals = new double[getCol()];
		for (int c = 0; c < getCol(); ++c)
			rVals[c] = getVal(r, c);
		return rVals;
	}
	
	public double[] getColVec(int c) 
	{ 
		double[] cVals = new double[getRow()];
		for (int r = 0; r < getRow(); ++r)
			cVals[r] = getVal(r, c);
		return cVals;
	}
	
	public double getVal(int r, int c)
	{
		return isRMjr_ ? mtrx_[r][c] : mtrx_[c][r];
	}
	
	public void setVal(int r, int c, double v)
	{
		if (isRMjr_) mtrx_[r][c] = v; else mtrx_[c][r] = v;
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
		if (m2.getCol() != result.getCol())
			throw new IncompatibleMatrixExpt(m2, result, "the column size of result matrix is not the same as multiplicand's");
		
		for (int r = 0; r < m1.getRow(); ++r)
			for (int c = 0; c < m2.getCol(); ++c) {
				double v = 0.0;
				for (int k = 0; k < m1.getCol(); ++k)
					v += m1.getVal(r, k)*m2.getVal(k, c);
				result.setVal(r, c, v);
			}
	}
	
	// Multiply transpose(m1) with m2
	public static void multiply_trans1(Matrix m1, Matrix m2, Matrix result) throws IncompatibleMatrixExpt
	{
		if (m1.getRow() != m2.getRow())
			throw new IncompatibleMatrixExpt(m1, m2, "multiply_trans1 matrices");
		if (m1.getCol() != result.getRow())
			throw new IncompatibleMatrixExpt(m1, result, "the row size of result matrix is not the same as multiplier's column");
		if (m2.getCol() != result.getCol())
			throw new IncompatibleMatrixExpt(m2, result, "the column size of result matrix is not the same as multiplicand's");		
	
		for (int r = 0; r < m1.getCol(); ++r)
			for (int c = 0; c < m2.getCol(); ++c) {
				double v = 0.0;
				for (int k = 0; k < m1.getRow(); ++k)
					v += m1.getVal(k, r)*m2.getVal(k, c);
				result.setVal(r, c, v);
			}
	}
	
	// Multiply m1 with transpose(m2)
	public static void multiply_trans2(Matrix m1, Matrix m2, Matrix result) throws IncompatibleMatrixExpt
	{
		if (m1.getCol() != m2.getCol())
			throw new IncompatibleMatrixExpt(m1, m2, "multiply_trans2 matrices");
		if (m1.getRow() != result.getRow())
			throw new IncompatibleMatrixExpt(m1, result, "the row size of result matrix is not the same as multiplier's");
		if (m2.getRow() != result.getCol())
			throw new IncompatibleMatrixExpt(m2, result, "the column size of result matrix is not the same as multiplicand's row");		
	
		for (int r = 0; r < m1.getRow(); ++r)
			for (int c = 0; c < m2.getRow(); ++c) {
				double v = 0.0;
				for (int k = 0; k < m1.getCol(); ++k)
					v += m1.getVal(r, k)*m2.getVal(c, k);
				result.setVal(r, c, v);
			}
	}
	
	public static void multiply(double coeff, Matrix m)
	{
		for (int r = 0; r < m.getRow(); ++r)
			for (int c = 0; c < m.getCol(); ++c)
				m.setVal(r, c, coeff*m.getVal(r, c));
	}
	
	public static void multiply(double[] rowVec, Matrix mtrx, double[] outVec) throws Exception
	{
		if (rowVec.length != mtrx.getRow() || mtrx.getCol() != outVec.length)
			throw new Exception("cannot multiply vector with matrix. Wrong size of vector or matrix");
		
		for (int i = 0; i < outVec.length; ++i) {
			outVec[i] = 0.0;
			for (int j = 0; j < rowVec.length; ++j)
				outVec[i] += rowVec[j]*mtrx.getVal(j, i);
		}
	}
	
	public static void multiply(Matrix mtrx, double[] colVec, double[] outVec) throws Exception
	{
		if (mtrx.getCol() != colVec.length || mtrx.getRow() != outVec.length)
			throw new Exception("cannot multiply matrix with vector. Wrong size of vector or matrix");
		
		for (int i = 0; i < outVec.length; ++i) {
			outVec[i] = 0.0;
			for (int j = 0; j < colVec.length; ++j)
				outVec[i] += mtrx.getVal(i, j)*colVec[j];
		}
	}
		
	public static Matrix copy(Matrix mtrx) throws IncompatibleMatrixExpt
	{
		Matrix targ = new Matrix(mtrx.getRow(), mtrx.getCol());
		for (int r = 0; r < mtrx.getRow(); ++r)
			for (int c = 0; c < mtrx.getCol(); ++c)
				targ.setVal(r, c, mtrx.getVal(r, c));
		return targ;
	}
	
	public static void transpose(Matrix mtrx, Matrix targ) throws IncompatibleMatrixExpt
	{
		if (mtrx.getRow() != targ.getCol() || mtrx.getCol() != targ.getRow())
			throw new IncompatibleMatrixExpt(mtrx, targ, "transpose matrix");
		for (int r = 0; r < targ.getRow(); ++r)
			for (int c = 0; c < targ.getCol(); ++c)
				targ.setVal(r, c, mtrx.getVal(c, r));	
	}
	
	public static int[] LUPDecompose(Matrix mtrx) throws NotSquareMatrix, SingularMatrixExpt
	{
		if (mtrx.getRow() != mtrx.getCol())
			throw new NotSquareMatrix(mtrx, "LUP Decomposition");
		
		int n = mtrx.getRow();
		int[] permVec = new int[n];
		// Initialize the permutation vector
		for (int i = 0; i < n; ++i)
			permVec[i] = i;
		
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
				int tempi = permVec[i];
				permVec[i] = permVec[maxR];
				permVec[maxR] = tempi;
				
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
	
	public static void LUSolve(Matrix LUMtrx, double[] b) throws NotSquareMatrix
	{
		if (LUMtrx.getRow() != LUMtrx.getCol())
			throw new NotSquareMatrix(LUMtrx, "LUSolve");
		
		int n = LUMtrx.getRow();
		// Forward substitution (using L)
		for (int i = 0; i < n; ++i) {
			double delSum = 0.0;
			for (int j = 0; j < i; ++j)
				delSum += LUMtrx.getVal(i, j)*b[j];
			b[i] = b[i]-delSum;
		}
		
		// Back substitution (using U)
		for (int i = n-1; i >= 0; --i) {
			double delSum = 0.0;
			for (int j = i+1; j < n; ++j)
				delSum += LUMtrx.getVal(i, j)*b[j];
			b[i] = (b[i] - delSum)/LUMtrx.getVal(i, i);
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
		int[] permVec = LUPDecompose(mtrx);
		
		int n = mtrx.getRow();
		for (int i = 0; i < n; ++i) {
			// solve the equation for the permVec[i]'th column of the result matrix
			double[] b = new double[n];
			for (int j=0; j < n; ++j)
				b[j] = (j==i ? 1.0: 0.0);
			LUSolve(mtrx, b);
			// put the result into the permVec[i]'th column of the result matrix
			int col = permVec[i];
			for (int r = 0; r < n; ++r)
				result.setVal(r, col, b[r]);
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
	
	public static Matrix identityMatrix(int n)
	{
		Matrix m = new Matrix(n, n);
		for (int i = 0; i < n; ++i)
			m.setVal(i, i, 1.0);
		return m;
	}
	
}


