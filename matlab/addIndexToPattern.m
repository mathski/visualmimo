function [pattern] = addIndexToPattern(messagepattern, index)
% adds the index three times to the pattern
index = dec2bin(index, 4) - '0';

pattern = [index index messagepattern index];

end