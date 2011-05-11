function symMulTest(symData, data)

import edu.uconn.vstlf.*;

% test symmetric multimplication symData*data

[x1, ~] = size(symData);
[~, y2] = size(data);
x = x1;
y = y2;

result = matrix.Matrix(x, y);
mtrx1 = matrix.Matrix(symData);
mtrx2 = matrix.Matrix(data);
matrix.Matrix.symmultiply(true, mtrx1, mtrx2, result);

refResult = symData*data;

for i = 1:x
    for j = 1:y
        assert( refResult(i, j) == result.getVal(i-1, j-1) );
    end
end