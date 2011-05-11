function [FirstMinHr,SecondMinHr,FirstMaxHr,SecondMaxHr]= CalLocalMinMax(x)

% A small test
% clear;
% clc;
% Origtemp=xlsread('d:\orig.xls');
% Org=reshape(Origtemp',1,[]);
% dayload=Org(1:184032-288*2);
% dayloadblock=reshape(dayload,[],91);
%  
% SuLoad=dayloadblock(288*0+1:288*1,:);
% MoLoad=dayloadblock(288*1+1:288*2,:);
% TuLoad=dayloadblock(288*2+1:288*3,:);
% WeLoad=dayloadblock(288*3+1:288*4,:);
% ThLoad=dayloadblock(288*4+1:288*5,:);
% FrLoad=dayloadblock(288*5+1:288*6,:);
% SaLoad=dayloadblock(288*6+1:288*7,:);
%  
% SuLoadAve=[];for i=1:288 SuLoadAve=[SuLoadAve,mean(SuLoad(i,:))]; end
% MoLoadAve=[];for i=1:288 MoLoadAve=[MoLoadAve,mean(MoLoad(i,:))]; end
% TuLoadAve=[];for i=1:288 TuLoadAve=[TuLoadAve,mean(TuLoad(i,:))]; end
% WeLoadAve=[];for i=1:288 WeLoadAve=[WeLoadAve,mean(WeLoad(i,:))]; end
% ThLoadAve=[];for i=1:288 ThLoadAve=[ThLoadAve,mean(ThLoad(i,:))]; end
% FrLoadAve=[];for i=1:288 FrLoadAve=[FrLoadAve,mean(FrLoad(i,:))]; end
% SaLoadAve=[];for i=1:288 SaLoadAve=[SaLoadAve,mean(SaLoad(i,:))]; end
%  
% v = SaLoadAve'; 
v=x;
N = length(v); 
 
% Calculate local min and max elements 
t = 0:length(v)-1; 
Lmax = diff(sign(diff(v)))== -2; % logic vector for the local max value 
Lmin = diff(sign(diff(v)))== 2; % logic vector for the local min value 
% match the logic vector to the original vecor to have the same length 
Lmax = [false; Lmax; false]; 
Lmin =  [false; Lmin; false]; 
tmax = t (Lmax); % locations of the local max elements 
tmin = t (Lmin); % locations of the local min elements 
vmax = v (Lmax); % values of the local max elements 
vmin = v (Lmin); % values of the local min elements 

% Obtain the first and second min and max elements
Tfirstmin=[];
Vfirstmin=[];
Tsecondmin=[];
Vsecondmin=[];
v=[];
t=[];
ttmax=[];
vvmax=[];

for i=1:length(tmax)
    if tmax(i)>190
        ttmax=[ttmax,tmax(i)];
        vvmax=[vvmax,vmax(i)];
    end 
end
[v,t]=sort(ttmax,'descend');

index=t(1);
for i=1:length(tmin)
    if tmin(i)<96
        Tfirstmin=[Tfirstmin,tmin(i)];
        Vfirstmin=[Vfirstmin,vmin(i)];
    elseif (tmin(i)>96 && tmin(i)<ttmax(index))
        Tsecondmin=[Tsecondmin,tmin(i)];
        Vsecondmin=[Vsecondmin,vmin(i)];
    end 
end 
v=[];
t=[];
[v,t]=sort(Vfirstmin);
index=t(1);
FirstMinHr=floor(Tfirstmin(index)/12);
v=[];
t=[];
[v,t]=sort(Vsecondmin);
index=t(1);
SecondMinHr=floor(Tsecondmin(index)/12);


Tfirstmax=[];
Vfirstmax=[];
Tsecondmax=[];
Vsecondmax=[];
for i=1:length(tmax)
    if ((tmax(i)>=96) && (tmax(i)<=Tsecondmin(index)))  
        Tfirstmax=[Tfirstmax,tmax(i)];
        Vfirstmax=[Vfirstmax,vmax(i)];
    elseif ((tmax(i)>=Tsecondmin(index)))  
        Tsecondmax=[Tsecondmax,tmax(i)];
        Vsecondmax=[Vsecondmax,vmax(i)];
    end 
end 
v=[];
t=[];
[v,t]=sort(Vfirstmax,'descend');
index=t(1);
FirstMaxHr=floor(Tfirstmax(index)/12);
v=[];
t=[];
[v,t]=sort(Vsecondmax,'descend');
index=t(1);
SecondMaxHr=floor(Tsecondmax(index)/12);

