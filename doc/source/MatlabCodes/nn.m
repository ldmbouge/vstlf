% The NN model. It can be modified for different NN structure.
function y=nn(theta,x,ny)
[nx,N]=size(x);
ns=numel(theta);                            
nh=(ns-ny)/(nx+ny+1);                   % calculate number of hidden nodes
W1=reshape(theta(1:nh*(nx+1)),nh,[]);   % extract weights from theta
W2=reshape(theta(nh*(nx+1)+1:end),ny,[]); 
% the NN model
y=W2(:,1:nh)*tanh(W1(:,1:nx)*x+W1(:,nx+ones(1,N)))+W2(:,nh+ones(1,N));
y=y(:);                                 % correct vector orientation for EKF