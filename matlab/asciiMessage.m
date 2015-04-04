function [message] = asciiMessage(string, max_length, index)
% encodes a string as 1's and 0's, padded with zeros to a binary encoded
% index
message = [];
for i = 1:length(string)
    message = [message, dec2bin(uint8(string(i))) - '0'];
end

index_bits = dec2bin(index) - '0';

if max_length < length(message) + length(index_bits);
    error('Message and index are too long')
end

message = [message, zeros(1, max_length - length(message) - length(index_bits)), index_bits];
end