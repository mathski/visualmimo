function [ message ] = randomMessageSample()

numFrames=10000;
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


messages = {'abcdefghijk'; 'lmnopqrstuv'};
frames = [];
for i = 1:length(messages);
    message = asciiMessage(messages{i}, 80, i-1);
    
    check = messageEncoder( alpha, height, width, message );
    check=uint8(check);

    img1=img+check;
    img2=img-check;

    frames = [frames im2frame(img1,cmap)];
    frames = [frames im2frame(img2,cmap)];
end
h = gcf;
loc = [0,-30,0,0];

%saveAVI(frames);
movie(h,frames,numFrames,fps,loc);

%movie2avi(frames, 'checkerboardStones.avi', 'FPS', fps, 'compression', 'none', 'KEYFRAME', 1);

end
