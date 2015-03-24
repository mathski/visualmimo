function [accuracy] = checkAccuracy(source, capture)
% Returns percentage differing between source and capture.
accuracy = sum(not(xor(source, capture))) / length(capture);