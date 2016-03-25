function [message] = asciiMessage(string, max_length)
% encodes a string as 1's and 0's, padded with zeros to a binary encoded
message = [];
for i = 1:length(string)
    message = [message, dec2bin(uint8(string(i))) - '0'];
end

if max_length < length(message) + 4;
    error('Message and index are too long')
end

sync_bit = 0;

message = [message, zeros(1, max_length - length(message))];
end