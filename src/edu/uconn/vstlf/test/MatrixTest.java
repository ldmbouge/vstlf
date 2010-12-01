package edu.uconn.vstlf.test;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

import edu.uconn.vstlf.matrix.Matrix;


public class MatrixTest {
	@Test public void multTest()
	{		
		try {
			double[][] d1 = {{1.0, 0.0, 2.0}, {-1.0, 3.0, 1.0}};
			double[][] d2 = {{3.0, 1.0}, {2.0, 1.0}, {1.0, 0.0}};
			double[][] d3 = {{5.0, 1.0}, {4.0, 2.0}};
			Matrix m1 = new Matrix(d1);
			Matrix m2 = new Matrix(d2);
			Matrix m3 = new Matrix(d3);
			Matrix result = new Matrix(2, 2);
			Matrix.multiply(m1, m2, result);
			
			System.out.println(m3.toString());
			
			assertTrue(Matrix.equal(result, m3, 10e-8));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test public void LUPDecomposeTst()
	{
		try {
		double d[][] = {{2.0, 0.0, 2.0, 0.6},
				        {3.0, 3.0, 4.0, -2.0},
				        {5.0, 5.0, 4.0, 2.0},
				        {-1.0, -2.0, 3.4, -1.0}};
		Matrix m = new Matrix(d);
		double lu[][] = {{5.0, 5.0, 4.0, 2.0},
				         {0.4, -2.0, 0.4, -0.2},
				         {-0.2, 0.5, 4.0, -0.5},
				         {0.6, 0, 0.4, -3.0}};
		Matrix LU = new Matrix(lu);
		Vector<Integer> permRef = new Vector<Integer>();
		permRef.add(2);
		permRef.add(0);
		permRef.add(3);
		permRef.add(1);
		
		int[] permVec = Matrix.LUPDecompose(m);
		System.out.println(m);
		assertTrue(Matrix.equal(m, LU, 10e-8));
		for (int i = 0; i < permVec.length; ++i)
			assertTrue(permVec[i] == permRef.get(i));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test public void inverseTest()
	{
		try {
		double d[][] = {{1.0, 2.0, 3.0}, {0.0, 4.0, 5.0}, {1.0, 0.0, 6.0}};
		Matrix m = new Matrix(d);
		Matrix result = new Matrix(m.getRow(), m.getCol());
		Matrix.inverse(m, result);
		
		double r[][] = {{24.0, -12.0, -2.0}, {5.0, 3.0, -5.0}, {-4.0, 2.0, 4.0}};
		Matrix ref = new Matrix(r);
		Matrix.multiply(1.0/22.0, ref);
		
		System.out.println("Result: " + result);
		System.out.println("Ref" + ref);
		assertTrue(Matrix.equal(result, ref, 10e-8));
		
		double d1[][] = {{3.0, 5.0, 12.0, 3.0, -23.0},
		                 {5.0, 89.0, 0.0, 12.0, 66.0},
		                 {-20.0, -1.0, -0.0, 0.6, 71.0},
		                 {2.6, -2.6, 3.0, 100.0, 3.4},
		                 {33.0, -33.0, 10.0, 24.0, -4.0}};
		Matrix m1 = new Matrix(d1);
		result = new Matrix(m1.getRow(), m1.getCol());
		Matrix.inverse(m1, result);
		
		double r1[][] = {{-0.017836819371067,  0.009292951817202, -0.012817004007217, -0.006077749560082,  0.023227508113305},
				         {0.004875345730042,  0.008693243056048, -0.006849161280530,  0.000275587283737, -0.005933091061172},
				         {0.076672922156298, -0.000669815559993,  0.026210510219574, -0.004628525083829,  0.009381050937591},
				         {-0.001541617478807, -0.000088689323833, -0.000984273155749,  0.010365098112351, -0.001259588459136},
				         {-0.004942761566254,  0.002740922436541,  0.010385934119276, -0.001795752996976,  0.006470039778597}};
		ref = new Matrix(r1);
		assertTrue(Matrix.equal(result, ref, 010e-8));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
	}
	
	@Test public void Invert()
	{
		try {
			int n = 572;
			Matrix m = new Matrix(n, n);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j)
					m.setVal(i, j, Math.random());
			Matrix.inverse(m, new Matrix(n, n));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test public void Mult()
	{
		try {
			int n = 572; int k = 200;
			Matrix m1 = new Matrix(n, k);
			Matrix m2 = new Matrix(k, n);
			Matrix m2_col = new Matrix(k, n, false);
			Matrix result1 = new Matrix(n, n);
			Matrix result2 = new Matrix(n, n);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < k; ++j) {
					m1.setVal(i, j, Math.random());
				}
			for (int i = 0; i < k; ++i)
				for (int j = 0; j < n; ++j) {
					m2.setVal(i, j, Math.random());
					m2_col.setVal(i, j, m2.getVal(i, j));
				}
			
			Matrix.multiply(m1, m2, result1);
			Matrix.multiply(m1, m2_col, result2);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j)
					assertTrue(result1.getVal(i, j) == result2.getVal(i, j));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test public void MultTrans() throws Exception
	{

			int n = 572, m = 200, t = 572;
			Matrix m1 = new Matrix(n, m);
			Matrix m2 = new Matrix(n, m);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < m; ++j) {
					m1.setVal(i, j, Math.random());
				}
			
			for (int i = 0; i < t; ++i)
				for (int j = 0; j < m; ++j)
					m2.setVal(i, j, Math.random());
			
			Matrix m2_trans = new Matrix(m, t);
			Matrix.transpose(m2, m2_trans);
			
			Matrix r1 = new Matrix(n, t), r2 = new Matrix(n, t);
			Matrix.multiply(m1, m2_trans, r1);
			Matrix.multiply_trans2(m1, m2, r2);
			
			assertTrue(Matrix.equal(r1, r2, 0.0));
			
			int k = 1000;
			Matrix m3 = new Matrix(n, k);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < k; ++j)
					m3.setVal(i, j, Math.random());
			
			Matrix m1_trans = new Matrix(m, n);
			Matrix.transpose(m1, m1_trans);
			Matrix r3 = new Matrix(m, m), r4 = new Matrix(m, m);
			Matrix.multiply(m1_trans, m2, r3);
			Matrix.multiply_trans1(m1, m2, r4);
			
			assertTrue(Matrix.equal(r3, r4, 0.0));
			
	}
}
