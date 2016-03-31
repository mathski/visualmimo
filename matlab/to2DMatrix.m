function [message] = to2DMatrix(message)

A(1:8,1:10) = double(0);
k = 1;

i = 1;
for h = 1:8,
    for w = 1:10,
       temp = message(i);
      A(h,w) = temp;
      i = i + 1;
   end 
end


message = A
end