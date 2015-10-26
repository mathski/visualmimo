function [ message ] = randomMessageSample(imageId, message)

numFrames=10000;
fps = 10;
alpha = 10;

height = 448;
width = 560;

% height = 896;
% width = 1120;

if ~exist('imageId', 'var'), imageId = '5'; end
[img, cmap] = imread(fullfile('images', imageId),'jpeg');

%[height,width,~]=size(img);
img=imresize(img, [height width]);

% HSV conversion
img = rgb2hsv(img);
% Histogram Equalize Intensity Channel
% img(:,:,3) = histeq(img(:,:,3), 256);
% RGB conversion
img=hsv2rgb(img);
% img=uint8(img*255);


if exist('message', 'var')
    messages = {message};
else
    messages = {'abcdefghijk'};
end

% messages = {'abcdefghijk'; 'lmnopqrstuv'};

frames = [];
for i = 1:length(messages);
    message = asciiMessage(messages{i}, 80, i-1);
    
    sync_bit = mod(i, 2);
    if sync_bit == 0;
        sync_bit = -1;
    end

    img1=uint8(img * 255 + messageEncoder(alpha, height, width, 8, 10, message, 0, sync_bit));
    img2=uint8(img * 255 + messageEncoder(alpha, height, width, 8, 10, message, 1, sync_bit));
    
    frames = [frames im2frame(img1,cmap)];
    frames = [frames im2frame(img2,cmap)];
end

frames = repmat(frames, 1, 100);

v = VideoWriter('vmimo.avi');
v.Quality = 100;
v.FrameRate = fps;
open(v);
writeVideo(v, frames);
close(v);

% use mplayer -loop 0 vmimo.avi

end
