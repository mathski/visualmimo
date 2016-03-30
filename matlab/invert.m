function[inverted] = invert(message)

inverted = xor(message, ones(1, length(message)));

end