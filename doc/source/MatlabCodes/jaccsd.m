function [z,A]=jaccsd(fun,x, h)
% JACCSD Jacobian through complex step differentiation
% [z J] = jaccsd(f,x)
% z = f(x)
% J = f'(x)
%
z=fun(x);
n=numel(x);
m=numel(z);
A=zeros(m,n);
for k=1:n
    x1=x;
    x1(k)=x1(k)+h;
    A(:,k)=(fun(x1)-z)/h;
end