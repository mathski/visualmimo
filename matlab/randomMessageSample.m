function [ message ] = randomMessageSample()
% :2:totalFrames
numFrames=1000;
fps = 12;
alpha = 10;

height = 448;
width = 560;

% height = 896;
% width = 1120;

[img, cmap] = imread('5','jpeg');
%[height,width,~]=size(img);
img=imresize(img, [height width]);

% HSV conversion
img = rgb2hsv(img);
% Histogram Equalize Intensity Channel
img(:,:,3) = histeq(img(:,:,3), 256);
% RGB conversion
img=hsv2rgb(img);
img=uint8(img*255);


message = asciiMessage('abcdefghijk', 80);
sum(message)

check = messageEncoder( alpha, height, width, message );
check=uint8(check);


img1=check+img;
img2=img-check;

frames(1)=im2frame(img1,cmap);
frames(2)=im2frame(img2,cmap);
h = gcf;
loc = [0,-30,0,0];

%saveAVI(frames);
movie(h,frames,numFrames,fps,loc);

%movie2avi(frames, 'checkerboardStones.avi', 'FPS', fps, 'compression', 'none', 'KEYFRAME', 1);

end
