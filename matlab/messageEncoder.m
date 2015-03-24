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
        sign = message(i*widthnum + j + 1) * 2 - 1;
        
        %going to fill with solid value if sign is 1
        checkerboard( (height+1:height+heightstep), (width+1:width+widthstep), 1:3 ) = sign*kappa;
    end
    
end

if blur_flag,
blurred = [imheight,imwidth];
for i = 0:(heightnum-1);
    height = i * heightstep;    
    for j = 0:(widthnum-1);
        width = j * widthstep;
        
        %iterate through each pixel of message
        for y = width + 1 : width + widthstep,
            for x = height + 1 : height + heightstep,
                pixelvalue = 0;
                if x > height + 1,
                    if y > width + 1,
                        pixelvalue =  pixelvalue + checkerboard(x-1, y-1, 1)* mask(1, x-height, y-width);
                    end
                    if y > 0,
                        pixelvalue =  pixelvalue + checkerboard(x-1, y  , 1)* mask(4, x-height, y-width);
                    end
                    if y < imwidth,
                        pixelvalue =  pixelvalue + checkerboard(x-1, y+1, 1)* mask(7, x-height, y-width);
                    end
                end
                if x > 0,
                    if y > width + 1,
                        pixelvalue =  pixelvalue + checkerboard(x  , y-1, 1)* mask(2, x-height, y-width);
                    end
                    if y > 0,
                        pixelvalue =  pixelvalue + checkerboard(x  , y  , 1)* mask(5, x-height, y-width);
                    end
                    if y < imwidth,
                        pixelvalue =  pixelvalue + checkerboard(x  , y+1, 1)* mask(8, x-height, y-width);
                    end
                end
                if x < height + heightstep,
                    if y > width + 1,
                        pixelvalue =  pixelvalue + checkerboard(x+1, y-1, 1)* mask(3, x-height, y-width);
                    end
                    if y > 0,
                        pixelvalue =  pixelvalue + checkerboard(x+1, y  , 1)* mask(6, x-height, y-width);
                    end
                    if y < imwidth,
                        pixelvalue =  pixelvalue + checkerboard(x+1, y+1, 1)* mask(9, x-height, y-width);
                    end
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
    