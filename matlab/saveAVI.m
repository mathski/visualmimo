function [] = saveAVI(frames)

mov = [];

for i=1:1000
    mov=[mov frames(1)];
    mov=[mov frames(2)];
end


movie2avi(mov, 'out.avi', 'fps', 40, 'videoname', 'Visual MIMO');

end