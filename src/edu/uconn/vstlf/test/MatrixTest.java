package edu.uconn.vstlf.test;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

import edu.uconn.vstlf.matrix.Matrix;


public class MatrixTest {
	@Test public void multTest()
	{		
		try {
			float[][] d1 = {{1.0f, 0.0f, 2.0f}, {-1.0f, 3.0f, 1.0f}};
			float[][] d2 = {{3.0f, 1.0f}, {2.0f, 1.0f}, {1.0f, 0.0f}};
			float[][] d3 = {{5.0f, 1.0f}, {4.0f, 2.0f}};
			Matrix m1 = new Matrix(d1);
			Matrix m2 = new Matrix(d2);
			Matrix m3 = new Matrix(d3);
			Matrix result = new Matrix(2, 2);
			Matrix.multiply(false, false, m1, m2, result);
			
			System.out.println(m3.toString());
			
			float [][] A = { {1.0f, 2.0f, 2.0f}, {2.0f, 1.0f, 2.0f}, {2.0f, 2.0f, 1.0f}};
			float [][] B = { {2.0f, 3.0f, 4.0f, 5.0f}, {6.0f, 7.0f, 8.0f, 9.0f}, {10.0f, 11.0f, 12.0f, 13.0f}};
			Matrix a = new Matrix(A);
			Matrix b = new Matrix(B);
			Matrix c = new Matrix(A.length, B[0].length);
			Matrix.multiply(false, false, a, b, c);
			System.out.println(c);
			
			assertTrue(Matrix.equal(result, m3, 10e-8f));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test public void LUPDecomposeTst()
	{
		try {
		float d[][] = {{2.0f, 0.0f, 2.0f, 0.6f},
				        {3.0f, 3.0f, 4.0f, -2.0f},
				        {5.0f, 5.0f, 4.0f, 2.0f},
				        {-1.0f, -2.0f, 3.4f, -1.0f}};
		Matrix m = new Matrix(d);
		float lu[][] = {{5.0f, 5.0f, 4.0f, 2.0f},
				         {0.4f, -2.0f, 0.4f, -0.2f},
				         {-0.2f, 0.5f, 4.0f, -0.5f},
				         {0.6f, 0f, 0.4f, -3.0f}};
		Matrix LU = new Matrix(lu);
		Vector<Integer> permRef = new Vector<Integer>();
		permRef.add(2);
		permRef.add(0);
		permRef.add(3);
		permRef.add(1);
		
		int[] permVec = Matrix.LUPDecompose(m);
		System.out.println(m);
		assertTrue(Matrix.equal(m, LU, 10e-7f));
		for (int i = 0; i < permVec.length; ++i)
			assertTrue(permVec[i] == permRef.get(i));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Test public void inverseTest()
	{
		try {
		float d[][] = {{1.0f, 2.0f, 3.0f}, {0.0f, 4.0f, 5.0f}, {1.0f, 0.0f, 6.0f}};
		Matrix m = new Matrix(d);
		Matrix result = new Matrix(m.getRow(), m.getCol());
		Matrix.inverse(m, result);
		
		float r[][] = {{24.0f, -12.0f, -2.0f}, {5.0f, 3.0f, -5.0f}, {-4.0f, 2.0f, 4.0f}};
		Matrix ref = new Matrix(r);
		Matrix.multiply(1.0f/22.0f, ref);
		
		System.out.println("Result: " + result);
		System.out.println("Ref" + ref);
		assertTrue(Matrix.equal(result, ref, 10e-8f));
		
		float d1[][] = {{3.0f, 5.0f, 12.0f, 3.0f, -23.0f},
		                 {5.0f, 89.0f, 0.0f, 12.0f, 66.0f},
		                 {-20.0f, -1.0f, -0.0f, 0.6f, 71.0f},
		                 {2.6f, -2.6f, 3.0f, 100.0f, 3.4f},
		                 {33.0f, -33.0f, 10.0f, 24.0f, -4.0f}};
		Matrix m1 = new Matrix(d1);
		result = new Matrix(m1.getRow(), m1.getCol());
		Matrix.inverse(m1, result);
		
		float r1[][] = {{-0.017836819371067f,  0.009292951817202f, -0.012817004007217f, -0.006077749560082f,  0.023227508113305f},
				         {0.004875345730042f,  0.008693243056048f, -0.006849161280530f,  0.000275587283737f, -0.005933091061172f},
				         {0.076672922156298f, -0.000669815559993f,  0.026210510219574f, -0.004628525083829f,  0.009381050937591f},
				         {-0.001541617478807f, -0.000088689323833f, -0.000984273155749f,  0.010365098112351f, -0.001259588459136f},
				         {-0.004942761566254f,  0.002740922436541f,  0.010385934119276f, -0.001795752996976f,  0.006470039778597f}};
		ref = new Matrix(r1);
		assertTrue(Matrix.equal(result, ref, 010e-8f));
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
					m.setVal(i, j, (float)Math.random());
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
			Matrix m2_col = new Matrix(k, n);
			Matrix result1 = new Matrix(n, n);
			Matrix result2 = new Matrix(n, n);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < k; ++j) {
					m1.setVal(i, j, (float)Math.random());
				}
			for (int i = 0; i < k; ++i)
				for (int j = 0; j < n; ++j) {
					m2.setVal(i, j, (float)Math.random());
					m2_col.setVal(i, j, m2.getVal(i, j));
				}
			
			Matrix.multiply(false, false, m1, m2, result1);
			Matrix.multiply(false, false, m1, m2_col, result2);
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
					m1.setVal(i, j, (float)Math.random());
				}
			
			for (int i = 0; i < t; ++i)
				for (int j = 0; j < m; ++j)
					m2.setVal(i, j, (float)Math.random());
			
			Matrix m2_trans = new Matrix(m, t);
			Matrix.transpose(m2, m2_trans);
			
			Matrix r1 = new Matrix(n, t), r2 = new Matrix(n, t);
			Matrix.multiply(false, false, m1, m2_trans, r1);
			Matrix.multiply(false, true, m1, m2, r2);
			
			assertTrue(Matrix.equal(r1, r2, 0.0f));
			
			int k = 1000;
			Matrix m3 = new Matrix(n, k);
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < k; ++j)
					m3.setVal(i, j, (float)Math.random());
			
			Matrix m1_trans = new Matrix(m, n);
			Matrix.transpose(m1, m1_trans);
			Matrix r3 = new Matrix(m, m), r4 = new Matrix(m, m);
			Matrix.multiply(false, false, m1_trans, m2, r3);
			Matrix.multiply(true, false, m1, m2, r4);
			
			assertTrue(Matrix.equal(r3, r4, 0.0f));
			
	}
}
