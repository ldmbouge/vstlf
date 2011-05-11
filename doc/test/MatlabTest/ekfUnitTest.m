javaaddpath('/home/yuting/Projects/vstlf/bin');
javaaddpath('/home/yuting/Projects/vstlf/lib/blas_simple.jar');
javaaddpath('/home/yuting/Projects/vstlf/lib/blas.jar');
javaaddpath('/home/yuting/Projects/vstlf/lib/f2jutil.jar');

data = [[0.125, 0.25]; [-0.125, 0.25]];
coeff = 0.125;
mtrxBasicTest(data, coeff);

data = [[0.323243, 0.23234]; [0.556546, 0.98549]];
coeff = 0.324;
mtrxBasicTest(data, coeff);

symData = [[1.232, 0.111, 0.222];
           [0.111, 3.232, 0.783];
           [0.222, 0.783, 9.877]];
data = [[0.88934, 0.322232];
        [4.5334, 9.7372];
        [0.0032, 0.7764]];

symMulTest(symData, data);