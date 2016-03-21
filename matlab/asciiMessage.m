function [message] = asciiMessage(string, max_length)
% encodes a string as 1's and 0's, padded with zeros to a binary encoded
% leaves room for 4 parity bits and 12 index bits (enough for three ascii
% characters in triplicate).
if ~exist('max_length', 'var'), max_length = 80 - 4 - 12; end;
message = [];
for i = 1:length(string)
    message = [message, dec2bin(uint8(string(i))) - '0'];
end

if max_length < length(message);
    error('Message is too long')
end

sync_bit = 0;

message = [message, zeros(1, max_length - length(message))];
end