function [] = writeFrames(frames, filename, fps)
% Loops given frames to create a few seconds of video,
% then writes lossless AVI file.
% Play with `mplayer -loop 0 filename` to loop endlessly.

frames = repmat(frames, 1, 10);

v = VideoWriter(filename);
v.Quality = 100;
v.FrameRate = fps;
open(v);
writeVideo(v, frames);
close(v);

end