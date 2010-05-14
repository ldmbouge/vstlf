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

VSTLFIMMKFForecaster(StrTrainTime,EndTrainTime,StrValTime,EndValTime,OrgLoad);
