clc;
clear;
LoadData=0;
% ===============================================================
% Initialziation
% ===============================================================
StrTrainTime='1-October-2006';
EndTrainTime='31-December-2007';
StrValTime='1-January-2008';
EndValTime='30-June-2008';
NumTrainDays=datenum(EndTrainTime, 'dd-mmm-yyyy')-datenum(StrTrainTime, 'dd-mmm-yyyy')+1;
NumValDays=datenum(EndValTime, 'dd-mmm-yyyy')-datenum(StrValTime, 'dd-mmm-yyyy')+1;
% load five minute load
load OrgLoad;
% set up decompostion levels
DecomposedLevels=2;
FiveMinLoad=[OrgLoad(1:(NumTrainDays+NumValDays)*24*12)];

%%Decompose the load into 2 levels with the padding strategy considered.
% HLoad=[];
% LLoad=[];
% tempload=[];
% [L,H]=GetDecomposedComponents(FiveMinLoad(1:120), DecomposedLevels, 'db2');
% HLoad=H;
% LLoad=L(DecomposedLevels,:);
% for j=11:(NumTrainDays+NumValDays)*24
%     tempload=horzcat(FiveMinLoad((j-11)*12+1:j*12),FiveMinLoad(j*12+1:(j)*12));
%     [L,H]=GetDecomposedComponents(tempload, DecomposedLevels, 'db2');
%     Htemp=H;
%     HLoad=[HLoad,Htemp(:,length(Htemp(1,:))-(12-1):length(Htemp(1,:)))];
%     Ltemp=L(DecomposedLevels,:);
%     LLoad=[LLoad,Ltemp(length(Ltemp)-(12-1):length(Ltemp))];
% end
% save('LLoad.mat','LLoad');
% save('HLoad.mat','HLoad');
load LLoad;
load HLoad;

% LL Load
[LowForecastLoad,LowSD,LowMinErr,RIOutput]=VSTLFEKFLowForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,LLoad);
% [LowForecastLoad,LowSD,LowMinErr,RIOutput]=VSTLFUKFLowForecaster3(StrTrainTime,EndTrainTime,StrValTime,EndValTime,LLoad,LSMDLoadAct,LSMDLoadPred,LowHidNum,LowStopCreteria,NNNum,MinMaxTag);
% H Load
i=1; [HighForecastLoad(i,:),HighSD1,HighMinErr(i,:)]=VSTLFUKFHighForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,HLoad(i,:));
% LH Load
i=2; [HighForecastLoad(i,:),HighSD2,HighMinErr(i,:)]=VSTLFUKFHighForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,HLoad(i,:));
ForecastLoad=[];
ForecastLoad=LowForecastLoad+HighForecastLoad(1,:)+HighForecastLoad(2,:);
FinalSD=LowSD+HighSD1+HighSD2;
FiveMinLoad=OrgLoad(1:(NumTrainDays+NumValDays)*24*12);
ForecastLoadDif=ForecastLoad-FiveMinLoad(NumTrainDays*12*24+1:length(FiveMinLoad));
ForecastDifArray=[];ForecastDifArray=[ForecastDifArray;ForecastLoadDif];
mape=reshape(100.*ForecastLoadDif./FiveMinLoad(NumTrainDays*12*24+1:length(FiveMinLoad)),12,length(ForecastLoadDif)/12);
c=reshape(ForecastLoadDif,12,length(ForecastLoadDif)/12);
temp=[];
MinErr=[];
MinErr1=[];
MAPE=[];
for k=1:12
    temp=[temp,c(k,:)];
    MinErr(k)=mean(abs(temp));
    MinErr1(k)=mean(abs(c(k,:)));
    MAPE(k)=mean(abs(mape(k,:)));
    DEVIATION(k)=std(c(k,:));
    PreSD(k)=mean(abs(FinalSD(:,k)));
end
MinErr;
MinErr1
MAPE;
DEVIATION
PreSD