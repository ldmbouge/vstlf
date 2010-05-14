
function [A, D] = GetDecomposedComponents(signal, level, waveform)

w = waveform;
s = signal;
[c,l] = wavedec(s,level,w, 'sp1');

for i = 1:level
    A(i,:) = wrcoef('a',c,l,w,i);
    D(i,:) = wrcoef('d',c,l,w,i);
end

end