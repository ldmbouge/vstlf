n = 3;
nrhs = 5;
A = randn(n, n);
B = randn(n, nrhs);
X = A\B;

javaaddpath('/home/yuting/Projects/vstlf/uconn-vstlf.jar');
import edu.uconn.vstlf.*;
jA = matrix.Matrix(n, n);
jB = matrix.Matrix(n, nrhs);
jX = matrix.Matrix(n, nrhs);
jA.setArray(A);
jB.setArray(B);

matrix.Matrix.solveLinearEqu(false, jA, jB, jX);

jXAry = jX.getArray();
for i=1:n
    for j=1:nrhs
        assert( X(i,j) == jXAry(i,j) );
    end
end

% X = B*inv(A) that is X*A=B that is A'*X'=B'
n = 3;
nrhs = 5;
A = randn(n, n);
B = randn(nrhs, n);
X = B/A;

jA = matrix.Matrix(n, n);
jBTrans = matrix.Matrix(n, nrhs);
jXTrans = matrix.Matrix(n, nrhs);
jA.setArray(A);
jBTrans.setArray(B');

matrix.Matrix.solveLinearEqu(true, jA, jBTrans, jXTrans);

jXAry = jXTrans.getArray()';
for i=1:nrhs
    for j=1:n
        assert( X(i,j) == jXAry(i,j) );
    end
end
