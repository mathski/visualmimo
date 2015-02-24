function [ message ] = randomMessageSample()
% :2:totalFrames
numFrames=1000;
fps = 20;
alpha = 40;

% ensure equal number on and off
on_left = 10 * 8 / 2;
off_left = 10 * 8 / 2;

height = 420;
width = 560;

[img, cmap] = imread('chips.jpg','jpeg');
%[height,width,~]=size(img);
img=imresize(img, [height width]);

% HSV conversion
img = rgb2hsv(img);
% Histogram Equalize Intensity Channel
img(:,:,3) = histeq(img(:,:,3), 256);
% RGB conversion
img=hsv2rgb(img);
img=uint8(img*255);

% message = [];
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
%message =[0,1,0,0,1,0,1,0,1,1,1,0,1,1,0,0,1,1,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,1,1,1,1,0,0,0,0,1,1,1,1,0,1,1,0,1,1,1,1,0,0,1,0,1,1,1,0,0,0,0,1,1,1,0,0,1,0,1,1,0,1,1,1,0,0];
%message =[0,0,0,0,0,0,0,1,1,1,0,0,0,1,1,1,0,0,1,1,0,0,0,0,1,0,1,0,1,0,0,0,1,1,0,1,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,0,0,1,1,1,1,0,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1];
%message = round(rand(1, 10*8));
message = [1,1,1,0,0,1,1,0,1,1,1,0,1,1,1,1,0,1,0,0,1,1,1,0,1,0,1,1,1,0,0,0,1,1,1,1,1,0,1,0,0,1,1,1,0,1,1,0,0,0,1,0,1,1,0,0,1,1,1,1,0,0,1,1,1,0,1,0,0,0,1,0,0,0,0,1,1,0,0,1]
sum(message)

check = messageEncoder( alpha, height, width, message );
check=uint8(check);

img1=check+img;
img2=img-check;
frames(1)=im2frame(img1,cmap);
frames(2)=im2frame(img2,cmap);
h = gcf;
loc = [0,0,0,0];
movie(h,frames,numFrames,fps,loc);


%movie2avi(frames, 'checkerboardStones.avi', 'FPS', fps, 'compression', 'none', 'KEYFRAME', 1);

end
