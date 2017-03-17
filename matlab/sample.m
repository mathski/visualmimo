function [ message ] = sample(imageId, messages, alpha, fps, height, width)
% Sample main function that encodes a message as a checkerboard, applies it
% to an image, and writes the result to file.
% imageId: optional string corresponding to one of the images in `images/`.
%          defaults to '5'.
% message: optional cell array of strings giving message to embed
%          defaults to {'abcdefghij'}

if ~exist('alpha', 'var'), alpha = 30; end
if ~exist('fps', 'var'), fps = 10; end
if ~exist('height', 'var'), height = 448; end
if ~exist('width', 'var'), width = 560; end

% necessary for python
alpha = double(alpha);
fps = double(fps);
height = double(height);
width = double(width);

if ~exist('imageId', 'var'), imageId = '14'; end
[img, cmap] = imread(fullfile('images', imageId),'jpeg');
% [img, cmap] = imread(imageId,'jpeg');

%[height,width,~]=size(img);
% img=imresize(img, [height width]);

% HSV conversion
% img = rgb2hsv(img);
% % Histogram Equalize Intensity Channel
% img(:,:,3) = histeq(img(:,:,3), 256);
% % RGB conversion
% img=hsv2rgb(img);
% img=uint8(img*255);


if ~exist('messages', 'var')
    messages = 'abcdefghijkl';
end

% pads messages with .'s to reach multiple of 4
if mod(length(messages), 4) ~= 0;
    messages = [messages repmat('.', 1, 4-mod(length(messages),4))];
end

frames = [];
for i = 1:4:length(messages)-3;
    message = addIndexToPattern(asciiMessage(messages(i:i+3)), (i-1)/4);
    
    % sync_bit for in-progress multiframe implementation
    parity_bit = mod((i-1)/4, 2);
%     if parity_bit == 0;
%         parity_bit = -1;
%     end

%     img1=uint8(img * 255 + checkerboardEncoder(alpha, height, width, 8, 10, message, 0, parity_bit));
%     img2=uint8(img * 255 + checkerboardEncoder(alpha, height, width, 8, 10, message, 1, parity_bit));

%     whos message
    
    % message1 is all zeros with parity bit in the corners
    message1 = to2DMatrix(message, parity_bit, 0);
    % message2 is the message with parity bit in the corners
    message2 = to2DMatrix(message, parity_bit, 1);
    
    embed1 = embedControllerImage(img, message1, alpha);
    embed2 = embedControllerImage(img, message2, alpha);

%     figure(40), imshow(embed2)
    
    frames = [frames im2frame(embed1) im2frame(embed2)];
end


filename = strcat(imageId, '-', messages, '-', int2str(alpha), '-', int2str(fps),  '.avi');
writeFrames(frames, filename, fps);

end
