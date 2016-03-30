function [message] = to2DMatrix(message)

A(1:8,1:10) = double(0);
k = 1;

for i = 1:numel(A),
    A(i) = message(i);
    k = k + 1;
end;


message = A
end