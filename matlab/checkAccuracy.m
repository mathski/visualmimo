function [accuracy] = checkAccuracy(source, capture)

accuracy = sum(not(xor(source, capture))) / length(capture);