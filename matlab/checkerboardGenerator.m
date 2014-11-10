function [ checkerboard ] = checkerboardGenerator( kappa, imheight, imwidth, varargin )
%% Generates Checkerboard
checkerboard = [imheight,imwidth];

if (nargin < 4 )
    numSquares = 20;
else
    numSquares = varargin;
end

heightstep = floor(imheight/numSquares);
widthstep = floor(imwidth/numSquares);

sign = 1;

% Intensity Threshold

for i = 0:(numSquares-1);
    height = i * heightstep;
    sign = -sign;
    
    for j = 0:(numSquares-1);
        width = j * widthstep;
        sign = -sign; 
        checkerboard( (height+1:height+heightstep), (width+1:width+widthstep), 1:3 ) = sign*kappa;
    end
    
end

checkerboard = double(checkerboard);