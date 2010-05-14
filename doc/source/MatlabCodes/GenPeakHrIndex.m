function PeakHrIndex=GenPeakHrIndex(FirstMinHr,SecondMinHr,FirstMaxHr,SecondMaxHr)

PeakHrIndex=[0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0;
    0 0 0 0
    ];
PeakHrIndex(FirstMinHr,1)=1;
PeakHrIndex(FirstMaxHr,2)=1;
PeakHrIndex(SecondMinHr,3)=1;
PeakHrIndex(SecondMaxHr,4)=1;

