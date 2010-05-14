function [FocastLoad,highsd,MinErr]=VSTLFUKFHighForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,FiveMinLoad)

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
FiveMinLoadInc=FiveMinLoad;
FiveMinLoadAnchor=(FiveMinLoad-min(FiveMinLoad))./(max(FiveMinLoad)-min(FiveMinLoad));
MinShift=min(FiveMinLoad)-0.0000001;
MaxShift=max(FiveMinLoad)+0.0000001;
DifMaxMin=MaxShift-MinShift;
FiveMinLoadInc=(FiveMinLoadInc-MinShift)/DifMaxMin;

%========== NN Training Configuration========================
TraLastHrStr=7*24*12+1;                    %7(days) x 24(hr) x 12(five min) + 1
TraLastHrEnd=NumTrainDays*24*12-12;           %Training Days x 24(hr) x 12(five min) x 7(weeks)
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
TraIn=[TraIn,LastHourLoadInc,TraTimeIndex(:,1:31)];
%NN Training Outputs
TraOut=[];
TraOut=[TraOut,reshape(FiveMinLoadInc(TraLastHrStr+12:TraLastHrEnd+12),TraCol,TraRow)'];

%========== NN Validation Configuration======================
ValLastHrStr=(NumTrainDays)*24*12-12+1;       %Training Days x 24(hr) x 12(five min) + 1
ValLastHrEnd=length(FiveMinLoadInc)-12;
ValLastDayStr=ValLastHrStr-23*12;          %LastHrStart - 23(hour) x 12(five min)
ValLastDayEnd=ValLastHrEnd-23*12;          %LastHrEnd - 23(hour) x 12(five min)
ValLastWeekStr=ValLastHrStr-(7*24-1)*12;   %LastHrStart - ( 7(days) x 24(hr) - 1 ) x 12(five min)
ValLastWeekEnd=ValLastHrEnd-(7*24-1)*12;   %LastHrEnd - ( 7(days) x 24(hr) - 1 ) x 12(five min)
ValRow=(ValLastHrEnd-ValLastHrStr+1)/12;
ValCol=12;
%NN Validation Inputs
LastHourLoadInc=reshape(FiveMinLoadInc(ValLastHrStr:ValLastHrEnd),ValCol,ValRow)';
ValIn=[];
load ValTimeIndex;
ValIn=[ValIn,LastHourLoadInc,ValTimeIndex(:,1:31)];
%NN Validation Outputs
ValOut=reshape(FiveMinLoadInc(ValLastHrStr+12:ValLastHrEnd+12),ValCol,ValRow)';

%========== Start Training======================
InputNum=43;
OptHidNum=[];

S=[];
x=TraIn;
y=TraOut;
nh=10;
nx=43;
ny=12;
ns = nx * nh + nh + nh * ny + ny;
load PHKF;
Q=0.000001*eye(ns);
R=0.0001*eye(ny);

theta=randn(ns,1);
x=theta;
for TT=1:5
    for k=1:length(TraIn(:,1))
%             for k=length(TraIn(:,1))-1:length(TraIn(:,1))
        [x,P,S,e(k,:)]=nnukf3(x,P,TraIn(k,:)',TraOut(k,:)',Q,R);
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
    [x,P,S,e(i,:)]=nnukf3(x,P,ValIn(i,:)',ValOut(i,:)',Q,R);
    sd=sqrt(diag(S));
    Output1=reshape(Output,1,12)*DifMaxMin+MinShift;
    Output2=reshape(sd,1,12)*DifMaxMin;
    FocastLoad=[FocastLoad,Output1];
%     FocastLoadSD=[FocastLoadSD;Output2];
    FocastLoadSD(i,:)=Output2;
    Err1(1,i)=FiveMinLoad(ValLastHrStr-1+i*12+1)-Output1(1);
    for j=2:12
        Err1(j,i)=FiveMinLoad(ValLastHrStr-1+i*12+j)-Output1(j);
    end
end
[a,b]=min(OptHidNum);
temp=[];
MinErr=[];
highsd=FocastLoadSD;
for k=1:12
    MinErr(k)=mean(abs(Err1(k,:)));
    sd(k)=std(abs(Err1(k,:)));
    sda(k)=mean(abs(FocastLoadSD(:,k)));
end
MinErr
sd
sda

