function [ checkerboard ] = messageEncoder( kappa, imheight, imwidth, message, varargin )
%% Encodes 1D bit array
checkerboard = [imheight,imwidth];

blur_flag = 1;

heightnum = 8;
widthnum = 10;

heightstep = floor(imheight/heightnum);
widthstep = floor(imwidth/widthnum);

mask = MessageBlending(imwidth/widthnum);

sign = 1;

% Intensity Threshold

for i = 0:(heightnum-1);
    height = i * heightstep;    
    for j = 0:(widthnum-1);
        width = j * widthstep;
        
        %sign is 1 if corresponding part of message is true, else 0
        sign = message(i*widthnum + j + 1);
        
        %going to fill with solid value if sign is 1
        checkerboard( (height+1:height+heightstep), (width+1:width+widthstep), 1:3 ) = sign*kappa;
    end
    
end

% if we're blurring, we'll iterate through the pattern we just generated
% and generate a second blurred image
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
                    pixelvalue =  pixelvalue + message((i-1) * widthnum + (j-1) + 1) * mask(1, x-height, y-width)*kappa;
                    tot = tot + mask(1, x-height, y-width);
                end
                if j > 0,
                    pixelvalue =  pixelvalue + message((i  ) * widthnum + (j-1) + 1) * mask(4, x-height, y-width)*kappa;
                    tot = tot + mask(4, x-height, y-width);
                end
                if i < heightnum - 1 && j > 0,
                    pixelvalue =  pixelvalue + message((i+1) * widthnum + (j-1) + 1) * mask(7, x-height, y-width)*kappa;
                    tot = tot + mask(7, x-height, y-width);
                end


                if i > 0,
                    pixelvalue =  pixelvalue + message((i-1) * widthnum + (j  ) + 1) * mask(2, x-height, y-width)*kappa;
                    tot = tot + mask(2, x-height, y-width);
                end
                if 1,
                    pixelvalue =  pixelvalue + message((i  ) * widthnum + (j  ) + 1) * mask(5, x-height, y-width)*kappa;
                    tot = tot + mask(5, x-height, y-width);
                end
                if i < heightnum - 1,
                    pixelvalue =  pixelvalue + message((i+1) * widthnum + (j  ) + 1) * mask(8, x-height, y-width)*kappa;
                    tot = tot + mask(8, x-height, y-width);
                end


                if i > 0 && j < widthnum - 1,
                    pixelvalue =  pixelvalue + message((i-1) * widthnum + (j+1) + 1) * mask(3, x-height, y-width)*kappa;
                    tot = tot + mask(3, x-height, y-width);
                end
                if j < widthnum - 1,
                    pixelvalue =  pixelvalue + message((i  ) * widthnum + (j+1) + 1) * mask(6, x-height, y-width)*kappa;
                    tot = tot + mask(6, x-height, y-width);
                end
                if i < heightnum - 1 && j < widthnum - 1,
                    pixelvalue =  pixelvalue + message((i+1) * widthnum + (j+1) + 1) * mask(9, x-height, y-width)*kappa;
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
    