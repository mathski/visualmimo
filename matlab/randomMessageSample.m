function [ message ] = randomMessageSample()
% :2:totalFrames
numFrames=10000;
fps = 8;
alpha = 40;

% ensure equal number on and off
on_left = 10 * 8 / 2;
off_left = 10 * 8 / 2;

height = 448;
width = 560;

[img, cmap] = imread('5','jpeg');
%[height,width,~]=size(img);
img=imresize(img, [height width]);

% HSV conversion
img = rgb2hsv(img);
% Histogram Equalize Intensity Channel
%img(:,:,3) = histeq(img(:,:,3), 256);
% RGB conversion
img=hsv2rgb(img);
img=uint8(img*255);

% message = [];]
% for i=1:10*8
%     randed = round(rand(1));
%     if randed == 1
%         if on_left > 0
%             on_left = on_left - 1;
%         else
%             off_left = off_left - 1;
%             randed = 0;
%         end
%     else
%         if off_left > 0
%             off_left = off_left - 1;
%         else
%             on_left = on_left - 1;
%             randed = 1;
%         end
%     end
%     message = [message, randed];
% end
% 
% on_left
% off_left
messages = {'abcdefghijk'; 'lmnopqrstuv'};
frames = [];
for i = 1:length(messages);
    message = asciiMessage(messages{i}, 80);
    
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
