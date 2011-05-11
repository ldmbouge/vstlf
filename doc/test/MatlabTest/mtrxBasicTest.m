function mtrxBasicTest(data, coeff)

import edu.uconn.vstlf.*;


% Test the type conversion
[x, y] = size(data);

mtrx = matrix.Matrix(data);

for i = 1:x
    for j = 1:y
        assert( data(i,j) == mtrx.getVal(i-1,j-1) );
    end
end

% Test the addition
mtrx = matrix.Matrix(data);
mtrx1 = matrix.Matrix(data);

addData = data + data;
matrix.Matrix.add(mtrx, mtrx1);

for i = 1:x
    for j = 1:y
        assert( addData(i, j) == mtrx.getVal(i-1, j-1) );
    end
end

% Test the substraction
mtrx = matrix.Matrix(data);
mtrx1 = matrix.Matrix(data);

subData = data - data;
matrix.Matrix.subtract(mtrx, mtrx1);

for i = 1:x
    for j = 1:y
        assert( subData(i, j) == mtrx.getVal(i-1, j-1) );
    end
end


% Test the multiplication
mdata = coeff*data;
mtMtrx = matrix.Matrix(data);
matrix.Matrix.multiply(coeff, mtMtrx);
for i=1:x
    for j=1:y
        assert( mdata(i,j) == mtMtrx.getVal(i-1, j-1) );
    end
end
