function [ message ] = sample(imageId, messages, alpha, fps, height, width)
% Sample main function that encodes a message as a checkerboard, applies it
% to an image, and writes the result to file.
% imageId: optional string corresponding to one of the images in `images/`.
%          defaults to '5'.
% message: optional cell array of strings giving message to embed
%          defaults to {'abcdefghij'}

if ~exist('alpha', 'var'), alpha = 10; end
if ~exist('fps', 'var'), fps = 10; end
if ~exist('height', 'var'), height = 448; end
if ~exist('width', 'var'), width = 560; end

% necessary for python
alpha = double(alpha);
fps = double(fps);
height = double(height);
width = double(width);

if ~exist('imageId', 'var'), imageId = '5'; end
[img, cmap] = imread(fullfile('images', imageId),'jpeg');
% [img, cmap] = imread(imageId,'jpeg');

%[height,width,~]=size(img);
img=imresize(img, [height width]);

% HSV conversion
img = rgb2hsv(img);
% Histogram Equalize Intensity Channel
img(:,:,3) = histeq(img(:,:,3), 256);
% RGB conversion
img=hsv2rgb(img);
% img=uint8(img*255);


if ~exist('message', 'var')
    messages = {'abcdefghij', 'lmnopqrstu'};
end

frames = [];
totalmessage = '';
for i = 1:length(messages);
    totalmessage = strcat(totalmessage, messages{i});
    message = asciiMessage(messages{i}, 80, i-1);
    
    % sync_bit for in-progress multiframe implementation
    parity_bit = mod(i, 2);
    if parity_bit == 0;
        parity_bit = -1;
    end

     img1=uint8(img * 255 + checkerboardEncoder(alpha, height, width, 8, 10, message, 0, parity_bit));
     img2=uint8(img * 255 + checkerboardEncoder(alpha, height, width, 8, 10, message, 1, parity_bit));
    
%     img1=uint8(img * 255 + colorEncoder(img, height, width, message, 0, sync_bit));
%     img2=uint8(img * 255 + colorEncoder(img, height, width, message, 1, sync_bit));
    
    frames = [frames im2frame(img1,cmap)];
    frames = [frames im2frame(img2,cmap)];
end


filename = strcat(imageId, '-', totalmessage, '-', int2str(alpha), '-', int2str(fps),  '.avi');
writeFrames(frames, filename, fps);

end
