function [message] = asciiMessage(string, max_length)
% encodes a string as 1's and 0's
message = [];
for i = 1:length(string)
    message = [message, dec2bin(uint8(string(i))) - '0'];
end

if max_length < length(message)
    message = message(1 : max_length);
else
    message = [message, zeros(1, max_length-length(message))];
end
end