function VSTLFIMMKFForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,FiveMinLoad)
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

%============================================================
% Calculate Peak Hour Index
%============================================================
PeakHrIndex=[];
fmld=reshape(FiveMinLoad,288,[]);
for i=1:length(fmld)
    [FirstMinHr(i),SecondMinHr(i),FirstMaxHr(i),SecondMaxHr(i)]=CalLocalMinMax(fmld(:,i));
    a=GenPeakHrIndex(FirstMinHr(i),SecondMinHr(i),FirstMaxHr(i),SecondMaxHr(i));
    PeakHrIndex=[PeakHrIndex;a];
end
%========== NN Training Configuration========================
TraLastHrStr=7*24*12+1;                    %7(days) x 24(hr) x 12(five min) + 1
TraLastHrEnd=NumTrainDays*24*12-12;        %Traingning Days x 24(hr) x 12(five min)-12
TraLastDayStr=TraLastHrStr-23*12;          %LastHrStart - 23(hour) x 12(five min)
TraLastDayEnd=TraLastHrEnd-23*12;          %LastHrEnd - 23(hour) x 12(five min)
TraLastWeekStr=TraLastHrStr-(7*24-1)*12;   %LastHrStart - ( 7(days) x 24(hr) - 1 ) x 12(five min)
TraLastWeekEnd=TraLastHrEnd-(7*24-1)*12;   %LastHrEnd - ( 7(days) x 24(hr) - 1 ) x 12(five min)
TraRow=(TraLastHrEnd-TraLastHrStr+1)/12;
TraCol=12;
%NN Training Inputs
LastHourLoadInc=reshape(FiveMinLoadInc(TraLastHrStr:TraLastHrEnd),TraCol,TraRow)';
TraIn=[];
load TraTimeIndex
load MonthID
TraIn=[TraIn,LastHourLoadInc,TraTimeIndex(:,1:31)];
TraIn=[TraIn,PeakHrIndex(2:10800,:)];
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
ValIn=[ValIn,PeakHrIndex(10801:10800+4368,:) ];
%NN Validation Outputs
ValOut=reshape(FiveMinLoadInc(ValLastHrStr+12:ValLastHrEnd+12),ValCol,ValRow)';
%========== Start Training======================
in=TraIn;
y=TraOut;

nh=10;
nx=length(TraIn(1,:));
ny=12;
ns = nx * nh + nh + nh * ny + ny;
x=randn(ns,1); % x is the weight of NN
P=diag(ones(1,612));
% load PHKF;
% P=diag(diag(P));
% P=diag([diag(P)' .01.*ones(1,120)]);

% Q=diag(.00001*ones(1,nx * nh + nh+nh * ny + ny));

% load P;
% load RHKF
Q=0.00001*eye(ns);
R=0.00001*eye(ny);

Qm=0.000001*eye(nx);
% Q=0.01*eye(ns);

TransPr = [0.9 0.5;0.5 0.9];
modePr=[0.5  0.5]';

for j=1:2

   modevm(:,j) = zeros(ns,1);
   modewm(:,j) = zeros(ny,1);

end
   modeR(:,1) = 1.*R(:);
   modeR(:,2) = 1.*R(:);
   modeQ(:,1) = 1.*Q(:);
   modeQ(:,2) = 1.*Q(:);
   modeQm(:,1) = 1*Qm(:);
   modeQm(:,2) = 1.*Qm(:);
for j=1:2
    modex(:,j)=x(:);
    modeP(:,j)=P(:);
end

for TT=1:1
    for k=1:length(TraIn(:,1))
%         for k=length(TraIn(:,1))-1:length(TraIn(:,1))
        [x,P,S,modex,modeP,modePr,modexp,modePP,modeS,modeK,modezp,modenu...
            ]=immhkf(TraIn(k,:)',modex,modeP,TraOut(k,:)',modeQ,modeQm,modeR,modevm,modewm,TransPr,modePr);
    end
end
%========== Start Validation====================
FocastLoad=[];
Err=[];
Err1=[];
temp=[];
SDArray=[];
SDArray1=[];
LastLoad1=[];
FocastLoadSD=[];
tempvalin=[];
for i=1:length(ValIn)
%          for i=length(ValIn)-1:length(ValIn)
    W1=reshape(x(1:nx * nh + nh),nh,[]);
    W2=reshape(x(nx * nh + nh+1:end),12,[]);
    Output=W2(:,1:nh)*tanh(W1(:,1:nx)*ValIn(i,:)'+W1(:,nx+1))+W2(:,nh+1);
    [x,P,S,modex,modeP,modePr,modexp,modePP,modeS,modeK,modezp,modenu...
        ]=immhkf(ValIn(i,:)',modex,modeP,ValOut(i,:)',modeQ,modeQm,modeR,modevm,modewm,TransPr,modePr);
    cov=diag(S);
    Output1=reshape(Output,1,12)*DifMaxMin+MinShift;
    Output0(i,:)=Output1;
    sdld=[];
    tempcov=[];
    for k=1:12
        for j=1:k
            tempcov(j)=((1+(filternorm(Output1(1:k),2)).^2)-Output1(j).^2)*cov(j);
        end
        sdld(k)=sqrt(sum(tempcov));
    end
    sdload=FiveMinLoad(ValLastHrStr-1+i*12)*DifMaxMin*sdld;
    FocastLoadSD(i,:)=sdload;
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

d=abs(Err1);
FSD=FocastLoadSD';
goodpred=zeros(1,12);
for k=1:12
    for t=1:4368
        if d(k,t)<=FSD(k,t)
            goodpred(k)=goodpred(k)+1;
        end;
    end
end
goodpred/4368*100

