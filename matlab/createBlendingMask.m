function [finalmask] = createBlendingMask(blocksize)
% Produces a blending mask.
% A message at pixel i,j is the weighted sum of the messages of 9 blocks (itself and
% all its neighbors). The 9 masks give the weights of each of the message
% blocks for every pixel. 

nblock = 3; % Don't change this
repeatsize = 5; % increases blurring between borders

blurfilt = conv2([1 4 6 4 1], [1 4 6 4 1]')/256;
for i = 1:repeatsize, blurfilt = conv2(blurfilt,blurfilt); end;

img = zeros(nblock*blocksize,nblock*blocksize);
border = 0;


zblock = zeros(blocksize,blocksize);
oblock = zeros(blocksize,blocksize);

for ii = 1:blocksize
    for jj = 1:blocksize
oblock(ii,jj) = (ii > border & jj > border & ii < (blocksize - border) & jj < (blocksize - border));
    end
end




mask = zeros(9,nblock*blocksize,nblock*blocksize);
mask(1,:,:) = [ oblock zblock zblock; zblock zblock zblock; zblock zblock zblock];
mask(2,:,:) = [ zblock oblock zblock; zblock zblock zblock; zblock zblock zblock];
mask(3,:,:) = [ zblock zblock oblock; zblock zblock zblock; zblock zblock zblock];
mask(4,:,:) = [ zblock zblock zblock; oblock zblock zblock; zblock zblock zblock];
mask(5,:,:) = [ zblock zblock zblock; zblock oblock zblock; zblock zblock zblock];
mask(6,:,:) = [ zblock zblock zblock; zblock zblock oblock; zblock zblock zblock];
mask(7,:,:) = [ zblock zblock zblock; zblock zblock zblock; oblock zblock zblock];
mask(8,:,:) = [ zblock zblock zblock; zblock zblock zblock; zblock oblock zblock];
mask(9,:,:) = [ zblock zblock zblock; zblock zblock zblock; zblock zblock oblock];

% Blurring
for i = 1:9,
    currentmask = reshape(mask(i,:,:),nblock*blocksize,nblock*blocksize); 
    currentmask = conv2(currentmask,blurfilt,'same');
    mask(i,:,:) = currentmask;
end;

 % The 9 masks encode the weight for each pixel ... normalize to that the
 % 9x1 vector per pixel as defined by the mask sums to 1
 
 ss  = nblock*blocksize;
 for (ii = 1:ss),
     for (jj = 1:ss)
        normfactor(ii,jj) = sum(mask(:,ii,jj));
     end
 end
 
 % for each pixel the nine masks define nine weights. Divide by normfactor
 % so they sum to 1. 
 
for ii = 1:9, 
    currentmask = reshape(mask(ii,:,:),nblock*blocksize,nblock*blocksize);
    currentmask = currentmask./normfactor;
    mask(ii,:,:) = currentmask;
end;


% Mask 1 defines the weights for upper left neighbor ...
%
%
% Mask 9 defines the weights for the lower right neighbor

% At this point we only need the weights in the center of the mask. 
% so crop off the rest
finalmask = mask(:,(blocksize+1):2*blocksize,(blocksize+1):2*blocksize);
end








