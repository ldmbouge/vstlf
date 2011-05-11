%IMMKF   Interacting Multiple Model algorithm with Kalman filters

function [x,P,S,modex,modeP,modePr,modexp,modePP,modeS,modeK,modezp,modenu...
    ]=immhkf(in,modex,modeP,z,modeQ,modeQm,modeR,modevm,modewm,TransPr,modePr)

% The COLUMNS of modeFfilename and modeHfilename store the names of f(x) and
% h(x) for each mode
% The COLUMNS of modedFfilename and modedHfilename store the names of df/dx and
% dh/dx for each mode
[nx,nm]=size(modex);
[nv,nm]=size(modevm);
[nw,nm]=size(modewm);
nz=length(z);
P=zeros(nx);
Qk1=zeros(nv);
Qm=zeros(length(in));
R=zeros(nw);
PP=P;

% interaction:
pred_modePr = TransPr'*modePr;
MixPr = TransPr.*(modePr*(pred_modePr.^(-1))');
modex0 = modex*MixPr;
for j=1:nm
    xk1=modex(:,j)-modex0(:,j);
    PP=xk1*xk1';
    modePP(:,j)=PP(:);
end
modeP = (modeP+modePP)*MixPr;

% filtering:
for j=1:nm
    x=modex0(:,j);
    P(:)=modeP(:,j);
    Qk1(:)=modeQ(:,j);
    Qm(:)=modeQm(:,j);
    R(:)=modeR(:,j);
%     um=sqrt(Qm)*randn(length(in),1);
%     um(13:43)=0;
%     vmk1=sqrt(Qk1)*randn(nv,1);
%     wm=sqrt(R)*randn(nw,1);
    vmk1=0;
    wm=0;
    um=0;
    f=@(u)u;                                % dumy process function to update parameters
    h=@(u)nn(u,in+um,size(z,1));                % NN model
%     g=@(u)nn(x,u,size(z,1));                % NN model
    if j==1
        [x,P,xk1,Pk1,S,K,zk1,nu]=ekf(in,x,P,z,Qk1,R,Qm,vmk1,um,wm,f,h);
%  [x,P,xk1,Pk1,S,K,zk1,nu]=ukf(in,x,P,z,Qk1,R,Qm,vmk1,um,wm,f,h);
    else
%                 [x,P,xk1,Pk1,S,K,zk1,nu]=ekf(in,x,P,z,Qk1,R,Qm,vmk1,um,wm,f,h);
        [x,P,xk1,Pk1,S,K,zk1,nu]=ukf(in,x,P,z,Qk1,R,Qm,vmk1,um,wm,f,h);
    end
    modex(:,j)=x;
    modeP(:,j)=P(:);
    modexp(:,j)=xk1;
    modePP(:,j)=Pk1(:);
    modeS(:,j)=S(:);
    modeK(:,j)=K(:);
    modezp(:,j)=zk1;
    modenu(:,j)=nu;
    %  likelihood(j)=gausspdf(nu,zeros(size(nu)),S);
    %  The above likelihood may experience an underflow problem.
    c(1,j)=pred_modePr(j)/sqrt(det(2*pi*S));
    c(2,j)=nu'*inv(S)*nu;
end


% mode probability calculation:
% modePr=pred_modePr.*likelihood';
% The above likelihood implementation may have an underflow problem.
% The following likelihood-ratio implementation is better numerically, which
% alleviates the underflow problem when the measurement residual is large:
for i=1:nm, dummy=0;
    for j=1:nm
        % if j~=i, dummy=dummy+c(1,j)*exp(-0.5*(c(2,j)/c(2,i))); end
        if j~=i, dummy=dummy+c(1,j)*exp(-0.5*(c(2,j)-c(2,i))); end % by xiangdong lin
    end
    % modePr(i)=c(1,i)/(c(1,i)+dummy);
    modePr(i)=1/(1+dummy/c(1,i));
end
modePr=modePr/sum(modePr);

% combination:
x = modex*modePr;
for j=1:nm
    xk1=modex(:,j)-x;
    PP=xk1*xk1';
    modePP(:,j)=PP(:);
end
P(:) = (modeP+modePP)*modePr;
S = reshape(modeS*modePr,12,12);
return


function [x,P,xkk_1,Pkk_1,S,W,zkk_1,nu]=ekf(in,xk_1k_1,Pk_1k_1,...
    z,Qk_1,R,Qm,vmk_1,um,wm,fstate,hmeas)
%EKF   Discrete-time first-order EKF for the following system:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%         plant equation:      x(k) = f[x(k-1)] + G(k-1)*v(k-1)              %
%         measurment equation: z(k) = h[x(k)]   + I(k)*w(k)                  %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%

[xkk_1,Fk_1]=jaccsd(fstate,xk_1k_1);
xkk_1=xkk_1+vmk_1;
Pkk_1 = Fk_1*Pk_1k_1*Fk_1' + Qk_1;
[zkk_1,H]=jaccsd(hmeas,xkk_1);
% [G]=jaccsd1(gmeas,in+um);
zkk_1=zkk_1+wm;
nu    = z - zkk_1;
S     = H*Pkk_1*H'+R;
W     = Pkk_1*H'*inv(S);
x     = xkk_1 + W*nu;
P     = (eye(size(W*H))-W*H)*Pkk_1*(eye(size(W*H))-W*H)'+W*R*W'; %Joseph Form
P     = (P + P')./ 2;
return


function [z,A]=jaccsd(fun,x)
% JACCSD Jacobian through complex step differentiation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% [z J] = jaccsd(f,x)
% z = f(x)
% J = f'(x)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
z=fun(x);
n=numel(x);
m=numel(z);
A=zeros(m,n);
h=n*eps;
for k=1:n
    x1=x;
    x1(k)=x1(k)+h*i;
    A(:,k)=imag(fun(x1))/h;
end

function [A]=jaccsd1(fun,in)

m=12;
t=numel(in);
A=zeros(m,t);
h=t*eps;
for k=1:t
    x1=in;%
    x1(k)=x1(k)+h*i;
    A(:,k)=imag(fun(x1))/h;
end



function y=nn(theta,x,ny)
% 
% Neural Networks
%
[nx,N]=size(x);
ns=numel(theta);
nh=(ns-ny)/(nx+ny+1);                   % calculate number of hidden nodes
W1=reshape(theta(1:nh*(nx+1)),nh,[]);   % extract weights from theta
W2=reshape(theta(nh*(nx+1)+1:end),ny,[]);
% the NN model
y=W2(:,1:nh)*tanh(W1(:,1:nx)*x+W1(:,nx+ones(1,N)))+W2(:,nh+ones(1,N));
y=y(:);


function [x,P,xkk_1,Pkk_1,S,W,zkk_1,nu]=ukf(in,xk_1k_1,Pk_1k_1,...
    z,Qk_1,R,Qm,vmk_1,um,wm,fstate,hmeas)

% [G]=jaccsd1(gmeas,in+um);
L=numel(xk_1k_1);                           %numer of states
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
X=sigmas(xk_1k_1,Pk_1k_1,c);                %sigma points around x
[xkk_1,X1,P1,X2]=ut(fstate,X,Wm,Wc,L,Qk_1); %unscented transformation of process
xkk_1=xkk_1+vmk_1;
% X1=sigmas(x1,P1,c);                       %sigma points around x1
% X2=X1-x1(:,ones(1,size(X1,2)));           %deviation of X1
[zkk_1,Z1,P2,Z2]=ut(hmeas,X1,Wm,Wc,m,R);    %unscented transformation of measurments
zkk_1=zkk_1+wm;
P12=X2*diag(Wc)*Z2';                        %transformed cross-covariance
K=P12*inv(P2);
x=xkk_1+K*(z-zkk_1);                        %state update
P=P1-K*P12';                                %covariance update

nu    = z - zkk_1;
W=K;
Pkk_1=P1;
S=P2;


function [y,Y,P,Y1]=ut(f,X,Wm,Wc,n,R)
%Unscented Transformation
%Input:
%        f: nonlinear map
%        X: sigma points
%       Wm: weights for mean
%       Wc: weights for covraiance
%        n: numer of outputs of f
%        R: additive covariance
%Output:
%        y: transformed mean
%        Y: transformed smapling points
%        P: transformed covariance
%       Y1: transformed deviations

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
%Sigma points around reference point
%Inputs:
%       x: reference point
%       P: covariance
%       c: coefficient
%Output:
%       X: Sigma points
A = c*chol(P)';
Y = x(:,ones(1,numel(x)));
X = [x Y+A Y-A];

