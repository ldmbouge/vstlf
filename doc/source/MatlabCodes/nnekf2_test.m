
function [theta,P,S]=nnekf2_test(theta,x,y,P,Q,R, wghChange)

f=@(u)u;                                % dumy process function to update parameters  
h=@(u)nn(u,x,size(y,1));                % NN model
in=x;    
[theta,P,S]=ekf(theta,y(:),P,Q,R,f,h, wghChange);    % the EKF
% e=h(theta);                             % returns trained model output

function [x,P,S]=ekf(x,z,P,Q,R,fstate,hmeas, h)
% [x1,A]=jaccsd(fstate,x);    %nonlinear update and linearization at current state
% P=A*P*A'+Q;                 %partial update
% [z1,H]=jaccsd(hmeas,x1);    %nonlinear measurement and linearization
% P12=P*H';                   %cross covariance
% % K=P12*inv(H*P12+R);       %Kalman filter gain
% % x=x1+K*(z-z1);            %state estimate
% % P=P-K*P12';               %state covariance matrix
% R=chol(H*P12+R);            %Cholesky factorization
% U=P12/R;                    %K=U/R'; Faster because of back substitution
% x=x1+U*(R'\(z-z1));         %Back substitution to get state update
% P=P-U*U';                   %Covariance update, U*U'=P12/R/R'*P12'=K*P12.
xk_1k_1=x;
Qk_1=Q;
Pk_1k_1=P;
[xkk_1,Fk_1]=jaccsd(fstate,xk_1k_1, h);
Pkk_1 = Fk_1*Pk_1k_1*Fk_1' + Qk_1;
[zkk_1,H]=jaccsd(hmeas,xkk_1, h);
nu    = z - zkk_1;
S     = H*Pkk_1*H' +R;
W     = Pkk_1*H'*inv(S);
x     = xkk_1 + W*nu;
P     = (eye(size(W*H))-W*H)*Pkk_1*(eye(size(W*H))-W*H)'+W*R*W'; %Joseph Form
P     = (P + P')./ 2;