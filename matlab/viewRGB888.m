function [ ] = viewRGB888( )
%Reads RGB888 array from file, views.
width = 800;
height = 480;

f = fopen('10.rgb888');
A = fread(f);

r = reshape(A, 3, width, height);
r = permute(r, [2,3,1]);

% Need to reduce intensity for some reason.
r = r .* 0.01;

% Flip horizontally
r = flipdim(r, 2);

imshow(r);

end

