function [ checkerboard ] = checkerboardEncoder( kappa, imheight, imwidth, heightnum, widthnum, message, invert, sync_bit)
% Encodes 1D bit array into a checkerboard pattern.
% kappa: intensity for 1's
% heightnum/widthnum: number of blocks in height and width
% message: binary string
% invert: if true, will produce an inverted image
% sync_bit: bit to be added at the end in the sync region, ignores invert

checkerboard = [imheight,imwidth];

% always blur since the accuracy loss is minimal
blur_flag = 1;

heightstep = floor(imheight/heightnum);
widthstep = floor(imwidth/widthnum);

mask = createBlendingMask(imwidth/widthnum);

if invert == 1;
    invert = -1;
else
    invert = 1;
end

% Intensity Threshold

for i = 0:(heightnum-1);
    height = i * heightstep;    
    for j = 0:(widthnum-1);
        width = j * widthstep;
        
        %sign is 1 or -1 if corresponding part of message is true, else 0
        sign = message(i*widthnum + j + 1) * invert;
        
        %going to fill with solid value if sign is 1
        checkerboard( (height+1:height+heightstep), (width+1:width+widthstep), 1:3 ) = sign*kappa;
    end
    
end

% if we're blurring, we'll iterate through the pattern we just generated
% and generate a second blurred image, where each pixel depends on value of
% all neighbor pixels and corresponding value in mask.
if blur_flag,
blurred = [imheight,imwidth];
for i = 0:(heightnum-1);
    height = i * heightstep;    
    for j = 0:(widthnum-1);
        width = j * widthstep;
        
        %iterate through each pixel of block
        for y = width + 1 : width + widthstep,
            for x = height + 1 : height + heightstep,
                % mask is the weights of the nine adjacent blocks for each
                % pixel in a block.
                % we sum the product of the message of adjacent blocks and
                % their weights, and this makes the new pixel's value
                pixelvalue = 0;
                tot = 0;
                if i > 0 && j > 0,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i-1, j-1, heightnum, widthnum, sync_bit, invert) * mask(1, x-height, y-width)*kappa;
                    tot = tot + mask(1, x-height, y-width);
                end
                if j > 0,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i, j-1, heightnum, widthnum, sync_bit, invert) * mask(4, x-height, y-width)*kappa;
                    tot = tot + mask(4, x-height, y-width);
                end
                if i < heightnum - 1 && j > 0,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i+1, j-1, heightnum, widthnum, sync_bit, invert) * mask(7, x-height, y-width)*kappa;
                    tot = tot + mask(7, x-height, y-width);
                end


                if i > 0,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i-1, j, heightnum, widthnum, sync_bit, invert) * mask(2, x-height, y-width)*kappa;
                    tot = tot + mask(2, x-height, y-width);
                end
                if 1,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i, j, heightnum, widthnum, sync_bit, invert) * mask(5, x-height, y-width)*kappa;
                    tot = tot + mask(5, x-height, y-width);
                end
                if i < heightnum - 1,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i+1, j, heightnum, widthnum, sync_bit, invert) * mask(8, x-height, y-width)*kappa;
                    tot = tot + mask(8, x-height, y-width);
                end


                if i > 0 && j < widthnum - 1,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i-1, j+1, heightnum, widthnum, sync_bit, invert) * mask(3, x-height, y-width)*kappa;
                    tot = tot + mask(3, x-height, y-width);
                end
                if j < widthnum - 1,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i, j+1, heightnum, widthnum, sync_bit, invert) * mask(6, x-height, y-width)*kappa;
                    tot = tot + mask(6, x-height, y-width);
                end
                if i < heightnum - 1 && j < widthnum - 1,
                    pixelvalue =  pixelvalue + getMessageAtPos(message, i+1, j+1, heightnum, widthnum, sync_bit, invert) * mask(9, x-height, y-width)*kappa;
                    tot = tot + mask(9, x-height, y-width);
                end

                % the weights are normalized to sum to 1, but edge blocks
                % are missing some neighbors so divide by total weights.
                if tot,
                    pixelvalue = pixelvalue / tot;
                end
                
                blurred(x, y, 1:3) = pixelvalue;
            end
        end
        
    end
end
checkerboard = imresize(double(blurred), [imheight, imwidth]);
else
checkerboard = imresize(double(checkerboard), [imheight, imwidth]);    
end

end

function [bit] = getMessageAtPos(message, i, j, heightnum, widthnum, sync_bit, invert)
% Returns the message bit at position i, j, inverted if appropriate,
% sign_bit if last bit (ignores invert for the sign bits)
    bit = message(i * widthnum + j + 1) * invert;
    
    if heightnum == i + 1 && widthnum == j + 1;
        bit = sync_bit;
    end
end