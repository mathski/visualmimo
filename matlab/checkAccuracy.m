function [accuracy] = checkAccuracy(source, capture)
% Returns percentage of bits differing between source and capture binary
% strings.
accuracy = sum(not(xor(source, capture))) / length(capture);