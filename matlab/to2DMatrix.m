function [matrix] = to2DMatrix(message, parity, base)

A(1:8,1:10) = double(0);

for h = 1:8,
    for w = 1:10,
      A(h,w) = getMessageAtPos(message, h-1, w-1, 8, 10, parity, base);
   end 
end


matrix = A
end

function [bit] = getMessageAtPos(message, i, j, heightnum, widthnum, parity_bit, base)
% Returns the message bit at position i, j
% If base, return blank frame with only parity bits.
% Top left and bottom right corners have the value of parity_bit
% Top right and bottom left corners have the value of -parity_bit
% Note that i and j are indexing from 0
    
    if (i == 0 && j == 0) || (i+1 == heightnum && j+1 == widthnum)
        bit = parity_bit;
    elseif (i+1 == heightnum && j == 0) || (i == 0 && j+1 == widthnum)
        bit = parity_bit;
    else
        parity_bit_offset = 1 + (i > 0) + (i+1 == heightnum);
        index = i * widthnum + j + 1 - parity_bit_offset;

        if base
            bit = 0;
        else
            bit = message(index);
        end
    end
end