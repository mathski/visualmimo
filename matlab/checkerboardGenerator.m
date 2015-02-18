function [ checkerboard ] = checkerboardGenerator( kappa, imheight, imwidth, varargin )
%% Generates Checkerboard
checkerboard = [imheight,imwidth];

heightnum = 8;
widthnum = 10;

heightstep = floor(imheight/heightnum);
widthstep = floor(imwidth/widthnum);

sign = 1;

% Intensity Threshold

for i = 0:(heightnum-1);
    height = i * heightstep;
    sign = -sign;
    
    for j = 0:(widthnum-1);
        width = j * widthstep;
        sign = -sign; 
        checkerboard( (height+1:height+heightstep), (width+1:width+widthstep), 1:3 ) = sign*kappa;
    end
    
end

checkerboard = imresize(double(checkerboard), [imheight, imwidth]);