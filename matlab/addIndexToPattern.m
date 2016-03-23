function [pattern] = addIndexToPattern(messagepattern, index)
% adds the index before and after the pattern, then pattern
index = dec2bin(index, 4) - '0';

pattern = [index messagepattern invert(index) invert(messagepattern)];

end