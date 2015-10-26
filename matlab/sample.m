function [ message ] = sample(imageId, message)
% Sample main function that encodes a message as a checkerboard, applies it
% to an image, and writes the result to file.
% imageId: optional string corresponding to one of the images in `images/`.
%          defaults to '5'.
% message: optional string giving message to embed
%          defaults to 'abcdefghijk'

alpha = 10;
fps = 10;

height = 448;
width = 560;

if ~exist('imageId', 'var'), imageId = '5'; end
[img, cmap] = imread(fullfile('images', imageId),'jpeg');

%[height,width,~]=size(img);
img=imresize(img, [height width]);

% HSV conversion
img = rgb2hsv(img);
% Histogram Equalize Intensity Channel
img(:,:,3) = histeq(img(:,:,3), 256);
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
    
    % sync_bit for in-progress multiframe implementation
    sync_bit = mod(i, 2);
    if sync_bit == 0;
        sync_bit = -1;
    end

    img1=uint8(img * 255 + messageEncoder(alpha, height, width, 8, 10, message, 0, sync_bit));
    img2=uint8(img * 255 + messageEncoder(alpha, height, width, 8, 10, message, 1, sync_bit));
    
    frames = [frames im2frame(img1,cmap)];
    frames = [frames im2frame(img2,cmap)];
end

writeFrames(frames, 'vmimo.avi', 10);

end
