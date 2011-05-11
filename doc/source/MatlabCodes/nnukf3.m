function [theta,P,S,e]=nnukf3(theta,P,x,y,Q,R)
%In UKF, theta is the state, x is the input and y is the output.
f=@(u)u;
h=@(u)nn(u,x,size(y,1));
[theta,P,S]=ukf(f,theta,y(:),P,h,Q,R);
e=h(theta);


function y=nn(theta,x,ny)
[nx,N]=size(x);
ns=numel(theta);
nh=(ns-ny)/(nx+ny+1);
W1=reshape(theta(1:nh*(nx+1)),nh,[]);
W2=reshape(theta(nh*(nx+1)+1:end),ny,[]);
y=W2(:,1:nh)*tanh(W1(:,1:nx)*x+W1(:,nx+ones(1,N)))+W2(:,nh+ones(1,N));
y=y(:);


function [x,P,S]=ukf(fstate,x,z,P,hmeas,Q,R)
L=numel(x);                                 %numer of states
m=numel(z);                                 %numer of measurements
alpha=1e-3;                                 %default, tunable
ki=0;                                       %default, tunable
beta=2;                                     %default, tunable
lambda=alpha^2*(L+ki)-L;                    %scaling factor
c=L+lambda;                                 %scaling factor
Wm=[lambda/c 0.5/c+zeros(1,2*L)];           %weights for means
Wc=Wm;
Wc(1)=Wc(1)+(1-alpha^2+beta);               %weights for covariance
c=sqrt(c);
X=sigmas(x,P,c);                            %sigma points around x
[x1,X1,P1,X2]=ut(fstate,X,Wm,Wc,L,Q);          %unscented transformation of process
% X1=sigmas(x1,P1,c);                         %sigma points around x1
% X2=X1-x1(:,ones(1,size(X1,2)));             %deviation of X1
[z1,Z1,P2,Z2]=ut(hmeas,X1,Wm,Wc,m,R);       %unscented transformation of measurments
P12=X2*diag(Wc)*Z2';                        %transformed cross-covariance
K=P12*inv(P2);
x=x1+K*(z-z1);                              %state update
P=P1-K*P12';                                %covariance update
S=P2;


function [y,Y,P,Y1]=ut(f,X,Wm,Wc,n,R)
L=size(X,2);
y=zeros(n,1);
Y=zeros(n,L);
for k=1:L                   
    Y(:,k)=f(X(:,k));       
    y=y+Wm(k)*Y(:,k);       
end
Y1=Y-y(:,ones(1,L));
P=Y1*diag(Wc)*Y1'+R;          


function X=sigmas(x,P,c)
A = c*chol(P)';
Y = x(:,ones(1,numel(x)));
X = [x Y+A Y-A]; 
