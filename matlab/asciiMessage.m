function [message] = asciiMessage(string, max_length)
% encodes a string as 1's and 0's, padded with zeros to a binary encoded
% leaves room for 4 parity bits and 8 index bits (enough for 4 ascii
% characters in duplicate).
if ~exist('max_length', 'var'), max_length = 80 - 4 - 8; end;
message = [];
for i = 1:length(string)
    message = [message, dec2bin(uint8(string(i))) - '0'];
end

if max_length < length(message) * 2;
    error('Message is too long')
end

message = [message, zeros(1, (max_length - length(message) * 2)/2)];
end