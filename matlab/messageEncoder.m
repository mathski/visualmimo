function [ checkerboard ] = messageEncoder( kappa, imheight, imwidth, message, varargin )
%% Encodes 1D bit array
checkerboard = [imheight,imwidth];

heightnum = 8;
widthnum = 10;

heightstep = floor(imheight/heightnum);
widthstep = floor(imwidth/widthnum);

sign = 1;

% Intensity Threshold

for i = 0:(heightnum-1);
    height = i * heightstep;
    
    for j = 0:(widthnum-1);
        width = j * widthstep;
        sign = message(i*widthnum + j + 1) * 2 - 1;
        checkerboard( (height+1:height+heightstep), (width+1:width+widthstep), 1:3 ) = sign*kappa;
    end
    
end

checkerboard = imresize(double(checkerboard), [imheight, imwidth]);