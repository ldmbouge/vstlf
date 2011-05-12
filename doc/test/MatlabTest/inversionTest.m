function [refInv, inved] = inversionTest(data)

import edu.uconn.vstlf.*;

% Test matrix inversion

[x, y] = size(data);
assert (x == y);
org = matrix.Matrix(data);
inved = matrix.Matrix(data);

matrix.Matrix.inverse(org, inved);

refInv = inv(data);

for i=1:x
    for j=1:y
        assert( refInv(i,j) == inved.getVal(i-1,j-1) );
    end
end