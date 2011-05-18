function [FocastLoad,lowsd,MinErr,RIOutput]=VSTLFEKFLowForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,FiveMinLoad)
NumTrainDays=datenum(EndTrainTime, 'dd-mmm-yyyy')-datenum(StrTrainTime, 'dd-mmm-yyyy')+1;
NumValDays=datenum(EndValTime, 'dd-mmm-yyyy')-datenum(StrValTime, 'dd-mmm-yyyy')+1;
WITra=weekday(StrTrainTime)-1;
HITra=1;
WIVal=weekday(StrValTime)-1;
PreWIVal=WIVal-1;
if PreWIVal<0;
    PreWIVal=6;
end
PreHIVal=24;

%============================================================
% Five Min load
%============================================================
a=FiveMinLoad(2:length(FiveMinLoad));
b=FiveMinLoad(1:(length(FiveMinLoad)-1));
FiveMinLoadInc=(a-b)./b;
temp=(FiveMinLoad(2)-FiveMinLoad(1))/FiveMinLoad(1);
FiveMinLoadInc=[temp,FiveMinLoadInc];
FiveMinLoadAnchor=(FiveMinLoad-min(FiveMinLoad))./(max(FiveMinLoad)-min(FiveMinLoad));
MinShift = -0.1;
MaxShift = +0.1;
DifMaxMin=MaxShift-MinShift;
FiveMinLoadInc=(FiveMinLoadInc-MinShift)/DifMaxMin;

%========== NN Training Configuration========================
TraLastHrStr=7*24*12+1;                    %7(days) x 24(hr) x 12(five min) + 1
TraLastHrEnd=NumTrainDays*24*12-12;           %Traingning Days x 24(hr) x 12(five min)-12
TraLastDayStr=TraLastHrStr-23*12;          %LastHrStart - 23(hour) x 12(five min)
TraLastDayEnd=TraLastHrEnd-23*12;          %LastHrEnd - 23(hour) x 12(five min)
TraLastWeekStr=TraLastHrStr-(7*24-1)*12;   %LastHrStart - ( 7(days) x 24(hr) - 1 ) x 12(five min)
TraLastWeekEnd=TraLastHrEnd-(7*24-1)*12;   %LastHrEnd - ( 7(days) x 24(hr) - 1 ) x 12(five min)
TraRow=(TraLastHrEnd-TraLastHrStr+1)/12;
TraCol=12;
%NN Training Inputs
LastHourLoadInc=reshape(FiveMinLoadInc(TraLastHrStr:TraLastHrEnd),TraCol,TraRow)';
LastDayLoadInc=reshape(FiveMinLoadInc(TraLastDayStr:TraLastDayEnd),TraCol,TraRow)';
TraIn=[];

load TraTimeIndex
load MonthID
TraIn=[TraIn,LastHourLoadInc,TraTimeIndex(:,1:31)];
%NN Training Outputs
TraOut=[];
TraOut=[TraOut,reshape(FiveMinLoadInc(TraLastHrStr+12:TraLastHrEnd+12),TraCol,TraRow)'];
%========== NN Validation Configuration======================
ValLastHrStr=(NumTrainDays)*24*12-12+1;       %Validation Days x 24(hr) x 12(five min) x 7 weeks + 1
ValLastHrEnd=length(FiveMinLoadInc)-12;
ValLastDayStr=ValLastHrStr-23*12;          %LastHrStart - 23(hour) x 12(five min)
ValLastDayEnd=ValLastHrEnd-23*12;          %LastHrEnd - 23(hour) x 12(five min)
ValLastWeekStr=ValLastHrStr-(7*24-1)*12;   %LastHrStart - ( 7(days) x 24(hr) - 1 ) x 12(five min)
ValLastWeekEnd=ValLastHrEnd-(7*24-1)*12;   %LastHrEnd - ( 7(days) x 24(hr) - 1 ) x 12(five min)
ValRow=(ValLastHrEnd-ValLastHrStr+1)/12;
ValCol=12;
%NN Validation Inputs
LastHourLoadInc=reshape(FiveMinLoadInc(ValLastHrStr:ValLastHrEnd),ValCol,ValRow)';
LastDayLoadInc=reshape(FiveMinLoadInc(ValLastDayStr:ValLastDayEnd),ValCol,ValRow)';
ValIn=[];
load ValTimeIndex;
ValIn=[ValIn,LastHourLoadInc,ValTimeIndex(:,1:31)];
%NN Validation Outputs
ValOut=reshape(FiveMinLoadInc(ValLastHrStr+12:ValLastHrEnd+12),ValCol,ValRow)';
%========== Start Training======================
InputNum=47;
OptHidNum=[];

in=TraIn;
y=TraOut;

nh=10;
nx=43;
ny=12;
ns = nx * nh + nh + nh * ny + ny;
x=randn(ns,1); % x is the weight of NN
load PHKF;
Q=0.000005*eye(ns);
R=0.00001*eye(ny);

javaaddpath('/home/yuting/Projects/vstlf/uconn-vstlf.jar');
import edu.uconn.vstlf.*;
jann = neuro.ekf.EKFANN([43, 10, 12]);
wChange = 1/2^15;
jann.setWeightChange(wChange);
jQ = matrix.Matrix(Q);
jR = matrix.Matrix(R);
jP = matrix.Matrix(P);
jx = javaArray('java.lang.Double', numel(x));
for i=1:numel(x)
    jx(i) = java.lang.Double(x(i,1));
end
    
for TT=1:10
    for k=1:length(TraIn(:,1))
%           for k=length(TraIn(:,1))-2:length(TraIn(:,1))
        tIn = TraIn(k,:)';
        tOut = TraOut(k,:)';
        jann.backwardPropTest(tIn, tOut, jx, jP, jQ, jR);
        [x,P,S]=nnekf2_test(x,tIn,tOut,P,Q,R, wChange);
        cnt = 0;
        for i=1:numel(x)
            if(abs(x(i,1) - jx(i).doubleValue()) > 10E-8)
                cnt = cnt+1;
            end
        end
        txt = ['iteration ', num2str(k), ': ', num2str(cnt), ' weights ', ' are ', ' different '];
        disp(txt);
    end
end
%========== Start Validation====================
FocastLoad=[];
Err=[];
Err1=[];
temp=[];
RIOutput=[];
SDArray=[];
SDArray1=[];
LastLoad1=[];
FocastLoadSD=[];
tempvalin=[];
for i=1:length(ValIn)
%     for i=length(ValIn)-1:length(ValIn)
    W1=reshape(x(1:nx * nh + nh),nh,[]);
    W2=reshape(x(nx * nh + nh+1:end),12,[]);
    Output=W2(:,1:nh)*tanh(W1(:,1:nx)*ValIn(i,:)'+W1(:,nx+1))+W2(:,nh+1);
    [x,P,S]=nnekf2(x,ValIn(i,:)',ValOut(i,:)',P,Q,R);
    Output1=reshape(Output,1,12)*DifMaxMin+MinShift;
    Output0(i,:)=Output1;
    cov=diag(S);
    sdld=[];
    tempcov=[];
    for k=1:12
        for j=1:k
            tempcov(j)=((1+(filternorm(Output1(1:k),2)).^2)-(Output1(j).^2))*cov(j);
        end
        sdld(k)=sqrt(sum(tempcov));
    end
    FocastLoadSD(i,:)=FiveMinLoad(ValLastHrStr-1+i*12)*DifMaxMin*sdld;
    temp=[temp,Output1];
    LastLoad=FiveMinLoad(ValLastHrStr-1+i*12)*(1+Output1(1));
    FocastLoad=[FocastLoad,LastLoad];
    Err=[Err,FiveMinLoad(ValLastHrStr-1+i*12+1)-LastLoad];
    Err1(1,i)=FiveMinLoad(ValLastHrStr-1+i*12+1)-LastLoad;
    for j=2:12
        LastLoad=LastLoad*(1+Output1(j));
        FocastLoad=[FocastLoad,LastLoad];
        Err=[Err,(FiveMinLoad(ValLastHrStr-1+i*12+j)-LastLoad)];
        Err1(j,i)=FiveMinLoad(ValLastHrStr-1+i*12+j)-LastLoad;
    end
end
temp=[];
temp1=[];
MinErr=[];
sda=[];
lowsd=FocastLoadSD;
for k=1:12
    MinErr(k)=mean(abs(Err1(k,:)));
    sd(k)=std(abs(Err1(k,:)));
    sda(k)=mean(abs(FocastLoadSD(:,k)));
end
MinErr
sd
sda
temp2=[];

