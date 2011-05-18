xk_1k_1=x;
Qk_1=Q;
Pk_1k_1=P;
xkk_1=xk_1k_1;
%[xkk_1,Fk_1]=jaccsd(fstate,xk_1k_1, h);
Pkk_1 = Pk_1k_1 + Qk_1;
hmeas=@(u)nn(u,tIn,size(tOut,1));
h=wChange;
[zkk_1,H]=jaccsd(hmeas,xkk_1, h);

wn = 572;
outn = 12;
jPtt1 = jP;
matrix.Matrix.add(jPtt1, jQ);
jHt = matrix.Matrix(outn, wn);
jann.setWeights(x);
z1 = jann.execute(tIn);
jann.jacobian(jHt);

for i=1:numel(z1)
    assert(zkk_1(i)==z1(i));
end

jHtAry = jHt.getArray();
[hrow, hcol] = size(H);
for i = 1:hrow
    for j = 1:hcol
        assert(H(i,j)==jHtAry(i,j));
    end
end

S     = H*Pkk_1*H' +R;

S_temp = matrix.Matrix(wn, outn);
S_t = matrix.Matrix(outn, outn);
matrix.Matrix.multiply(false, true, jPtt1, jHt, S_temp);

matrix.Matrix.multiply(false, false, jHt, S_temp, S_t);
matrix.Matrix.add(S_t, jR);

[row,col] = size(S);
S_t_ary=S_t.getArray();
for i=1:row
    for j=1:col
        assert(abs(S_t_ary(i,j)-S(i,j)) < 10E-14)
    end
end

W     = Pkk_1*H'*inv(S);

S_t_inv = matrix.Matrix(outn, outn);
K_t = matrix.Matrix(wn, outn);
matrix.Matrix.inverse(matrix.Matrix.copy(S_t), S_t_inv);
matrix.Matrix.multiply(false, false, S_temp, S_t_inv, K_t);

for i=1:row
    for j=1:col
        assert(abs(S_t_ary(i,j)-S(i,j)) < 10E-12)
    end
end

nu    = tOut - zkk_1;
x     = xkk_1 + W*nu;

uz = tOut-z1;
w_t_t= matrix.Matrix.multiply(K_t, uz);
w_t_t= w_t_t+xkk_1;
for i=1:numel(x)
    assert(abs(x(i)-w_t_t(i))<10E-13);
end

Imin = (eye(size(W*H))-W*H);

KHMult = matrix.Matrix(wn, wn);
matrix.Matrix.multiply(false, false, K_t, jHt, KHMult);
khmAry = KHMult.getArray();
for i=1:wn
    for j = 1:wn
        if i==j
            khmAry(i,j) = 1.0 - khmAry(i,j);
        else
            khmAry(i,j) = -khmAry(i,j);
        end
    end
end
KHMult.setArray(khmAry);
I_minus_KHMult = KHMult;

imkhwAry = I_minus_KHMult.getArray();
for i=1:wn
    for j=1:wn
        assert( abs(imkhwAry(i,j)-Imin(i,j)) < 10E-15 );
    end
end

P     = (eye(size(W*H))-W*H)*Pkk_1*(eye(size(W*H))-W*H)'+W*R*W'; %Joseph Form

RK_trans_mult = matrix.Matrix(outn, wn);
KRK_trans = matrix.Matrix(wn, wn);
P_temp_mult = matrix.Matrix(wn, wn);
P_temp = matrix.Matrix(wn, wn);
matrix.Matrix.multiply(false, true, jR, K_t, RK_trans_mult);
matrix.Matrix.multiply(false, false, K_t, RK_trans_mult, KRK_trans);
matrix.Matrix.multiply(false, true, jPtt1, I_minus_KHMult, P_temp_mult);
matrix.Matrix.multiply(false, false, I_minus_KHMult, P_temp_mult, P_temp);
matrix.Matrix.add(P_temp, KRK_trans);

PtempAry = P_temp.getArray();
for i=1:wn
    for j=1:wn
        assert( abs(PtempAry(i,j)-P(i,j)) < 10E14 );
    end
end
       
P     = (P + P')./ 2;

PtempAry = (PtempAry + PtempAry')./2;

jP.setArray(PtempAry);

for i=1:wn
    for j=1:wn
        assert( abs(PtempAry(i,j)-P(i,j)) < 10E14 );
    end
end