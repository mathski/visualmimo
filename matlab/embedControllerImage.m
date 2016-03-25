function  [apple_base, appleEmbedded] = embedControllerImage(apple, varargin)
%% [apple_base, appleEmbedded, mymap] = matchPixelsAndEmbedImage(INPUT_IMAGE, MESSAGE(NxM matrix), DELTA_SIZE, BASE_COLOR_MAP, DELTA_COLOR_MAP)
    % matchPixelsAndEmbedImage takes a base image and generates a pair of 
    % embedded images, along with a colormap used to make movie frames.

    %% Resize input image to convenient size
%   
    img_size = size(apple);
    disp(img_size);
    if(img_size >= [360,640,3])
        scale = max([360,640]./img_size(1:2));
        apple = imresize(apple,scale);
        apple = apple(1:360,1:640,:);
        apple = double(apple)/255;
    else
        disp('Image too small!')
        return
    end
    disp(size(apple));
    %{
    img_size = size(apple);
    if(img_size >= [1152,2644,3])
        scale = max([1152,2644]./img_size(1:2));
        apple = imresize(apple,scale);
        apple = apple(1:1152,1:2644,:);
        apple = double(apple)/255;
    else
        disp('Image too small!')
        return
    end
    %}
    %% Defaults and Variable Inputs
    [imgHeight,imgWidth,~]=size(apple);
    
    if nargin == 1 % Default message and step size
        %%% Checkerboard Message
        checkHeightBlocks = 8;
        checkWidthBlocks = 10;
        genCheck = double(checkerboard(1,checkHeightBlocks,checkWidthBlocks) > 0.5);
        message = genCheck(1:(end/2),1:(end/2));
        %%% Delta step size
        delta_length = 5;
        %%% recalculate colormap flag
        recal_colormap_flag = true;
    elseif nargin == 2 % Default Step Size
        %%% Checkerboard Message
        message = to2DMatrix(varargin{1});
        message1D = varargin{1};
        %%% Delta step size
        delta_length = 5;
        %%% recalculate colormap flag
        recal_colormap_flag = true;
    elseif nargin == 3 % Non Default Message and Step Size
        %%% Checkerboard Message
        message = varargin{1};
        %%% Delta step size
        delta_length = varargin{2};
        %%% recalculate colormap flag
        recal_colormap_flag = true;
    elseif nargin == 5 % NON-default colormaps, message, and step size
        %%% Checkerboard Message
        message = varargin{1};
        %%% Delta step size
        delta_length = varargin{2};
        %%% Base colormap
        mymap_base = varargin{3};
        %%% Delta colormap
        mymap_delta = varargin{4};
        %%% recalculate colormap flag
        recal_colormap_flag = false;
    else
        disp('Wrong number of inputs')
        return
    end
    delta_length = 40;
    
    [checkHeightBlocks,checkWidthBlocks] = size(message);
    checkPixelSize = imgHeight/checkHeightBlocks;
        
    disp(message);
    assignin('base', 'message', message);
    
    %% Load Training Data - Labeled Color Pairs
    if(recal_colormap_flag)
        BER_threshold = 0;
        BER_ONLY_FLAG = false;
        load Vgood.mat
        load Vbad.mat
        load k_50_ellipsoid_boost_100_samples.mat
        load k_50_ellipsoid_boost_100_samples_binary_flicker.mat
        load k_50_ellipsoid_boost_100_samples_camera_BER.mat
        [boostedGoodPoints1,~] = FixTrainingDataFormat(k_50_ellipsoid_boost_100_samples', k_50_ellipsoid_boost_100_samples_binary_flicker', k_50_ellipsoid_boost_100_samples_camera_BER', BER_threshold,BER_ONLY_FLAG);
        load k_50_means_ellipsoids_unpopulated_100_samples.mat
        load k_50_means_ellipsoids_unpopulated_100_samples_binary_flicker_re.mat
        load k_50_means_ellipsoids_unpopulated_100_samples_camera_BER.mat
        [boostedGoodPoints2,~] = FixTrainingDataFormat(k_50_means_ellipsoids_unpopulated_100_samples', k_50_means_ellipsoids_unpopulated_100_samples_binary_flicker_re', k_50_means_ellipsoids_unpopulated_100_samples_camera_BER', BER_threshold,BER_ONLY_FLAG);
        load k_50_ellipsoid_1000_samples.mat
        load k_50_ellipsoid_1000_samples_binary_flicker_results.mat
        load k_50_ellipsoid_1000_samples_camera_results.mat
        [boostedGoodPoints3,~] = FixTrainingDataFormat(k_50_ellipsoid_1000_samples', k_50_ellipsoid_1000_samples_binary_flicker_results', k_50_ellipsoid_1000_samples_camera_results', BER_threshold,BER_ONLY_FLAG);
        boostedGoodPoints = [boostedGoodPoints1; boostedGoodPoints2; boostedGoodPoints3];

        Vgood_all = [Vgood(:,1:6); boostedGoodPoints];

        % Normalize Delta Length
        Vgood_delta1 = Vgood_all(:,4)./sqrt(Vgood_all(:,4).^2+Vgood_all(:,5).^2+Vgood_all(:,6).^2);
        Vgood_delta2 = Vgood_all(:,5)./sqrt(Vgood_all(:,4).^2+Vgood_all(:,5).^2+Vgood_all(:,6).^2);
        Vgood_delta3 = Vgood_all(:,6)./sqrt(Vgood_all(:,4).^2+Vgood_all(:,5).^2+Vgood_all(:,6).^2);

        Vgood_all(:,4) = Vgood_delta1;
        Vgood_all(:,5) = Vgood_delta2;
        Vgood_all(:,6) = Vgood_delta3;
    
        end

    %% Map image colors to known colors
    if(recal_colormap_flag)
        good_values = Vgood_all;
        unique_map = unique(good_values(:,1:6),'rows')/255;
        mymap_base = unique_map(:,1:3);
    end

    appleind = rgb2ind(apple,mymap_base,'nodither');
    apple_base = ind2rgb(appleind,mymap_base);
    
    %% Embed normalized delta length
    %mymap_delta = unique_map(:,4:6) - unique_map(:,1:3);
    if(recal_colormap_flag)
        mymap_delta = unique_map(:,4:6);
    end
    apple_delta = ind2rgb(appleind,mymap_delta);
    
    %{
    delta_size = (delta_length/255);
    % NOT SUBTRACTING DELTA, INSTEAD OF ADDING
    for i = 1:length(mymap_delta)
        x = mymap_delta(i,1);
        y = mymap_delta(i,2);
        z = mymap_delta(i,3);
        individual_delta_length = delta_size ./ sqrt(x^2+y^2+z^2);
        mymap_delta(i,1:3) = individual_delta_length.*[x,y,z]';
    end
    
    apple_delta = ind2rgb(appleind,mymap_delta);
    %}

    
    %% Create Blended Checkrerboard Pattern
    % Create blending mask
    mask = createBlendingMask(64);
    heightstep  = 360/8;
    widthstep   = 640 / 10;
    widthnum    = 10;
    heightnum   = 8;

    blurred = zeros(imgHeight,imgWidth);
    
    for h = 0:(heightnum-1);
    height = h * heightstep;
    for w = 0:(widthnum-1);
    width = w * widthstep;
        %iterate through each pixel of block
        for y = width + 1 : width + widthstep,
        for x = height + 1 : height + heightstep,
            %fprintf('%d %d %d %d\n', x,y,w,h);
            % mask is the weights of the nine adjacent blocks for each
            % pixel in a block.
            % we sum the product of the message of adjacent blocks and
            % their weights, and this makes the new pixel's value
            pixelvalue = 0;
            tot = 0;
            if (h > 0) && (w > 0),
                pixelvalue =  pixelvalue + message((h-1) * heightnum + w-1 + 1) * mask(1, x-height, y-width);
                tot = tot + mask(1, x-height, y-width);
            end
            if w > 0,
                pixelvalue =  pixelvalue + message(h * heightnum + w-1 + 1) * mask(4, x-height, y-width);
                tot = tot + mask(4, x-height, y-width);
            end
            if (h < (heightnum - 1)) && (w > 0) && length(message) >= ((h+1) * heightnum + w-1 + 1),
                pixelvalue =  pixelvalue + message((h+1) * heightnum + w-1 + 1) * mask(7, x-height, y-width);
                tot = tot + mask(7, x-height, y-width);
            end

            if h > 0,
                pixelvalue =  pixelvalue + message((h-1) * heightnum + w + 1) * mask(2, x-height, y-width);
                tot = tot + mask(2, x-height, y-width);
            end
            if 1,
                pixelvalue =  pixelvalue + message(h * heightnum + w + 1) * mask(5, x-height, y-width);
                tot = tot + mask(5, x-height, y-width);
            end
            if h < (heightnum - 1) && length(message) >= ((h+1) * heightnum + w + 1),
                pixelvalue =  pixelvalue + message((h+1) * heightnum + w + 1) * mask(8, x-height, y-width);
                tot = tot + mask(8, x-height, y-width);
            end

            if (h > 0) && (w < (widthnum - 1)),
                pixelvalue =  pixelvalue + message((h-1) * heightnum + w+1 + 1) * mask(3, x-height, y-width);
                tot = tot + mask(3, x-height, y-width);
            end
            if w < (widthnum - 1),
                pixelvalue =  pixelvalue + message(h * heightnum + w+1 + 1) * mask(6, x-height, y-width);
                tot = tot + mask(6, x-height, y-width);
            end
            if (h < (heightnum - 1)) && (w < (widthnum - 1)) && length(message) >= ((h+1) * heightnum + w+1 + 1),
                pixelvalue =  pixelvalue + message((h+1) * heightnum + w+1 + 1) * mask(9, x-height, y-width);
                tot = tot + mask(9, x-height, y-width);
            end

            % the weights are normalized to sum to 1, but edge blocks
            % are missing some neighbors so divide by total weights.
            if tot,
                pixelvalue = pixelvalue / tot;
            end
            blurred(x,y) = pixelvalue;%*flip;
        end
        end
    end
    end
    
    %% Embedd checkerboard pattern via addition
    appleEmbedded = zeros(size(apple));
    whos apple_delta apple_base blurred;
    for i = 1:3 %For each of 3 RGB color channels
        appleEmbedded(:,:,i) = apple_base(:,:,i) + blurred.*apple_delta(:,:,i)*delta_length;    
    end
    
    %% Bound RGB values between [0.0 1.0] After Embedding
    appleEmbedded(appleEmbedded<0.0) = 0.0;
    appleEmbedded(appleEmbedded>1.0) = 1.0;

    
    figure, imshow(apple_base)'
    %% Generate video for human vision tests
    % :2:totalFrames
    %%{
    fps = 6;
    durration = 30; %video durration in seconds
    numFrames = fps*durration;

    prompt = ['\n - Press <Enter> to Play Video - \n'];
    input(prompt);

    imwrite(apple_base, 'vmimo1.bmp');
    imwrite(appleEmbedded, 'vmimo3.bmp');
    imwrite(apple_base, 'vmimo2.bmp');
    imwrite(appleEmbedded, 'vmimo4.bmp');
    
    frames(1) = im2frame(apple_base);
    frames(2) = im2frame(appleEmbedded);

    writeFrames(frames, 'sampleVid.avi', fps);
    
    figure(640), movie(frames,numFrames,fps);
    %%}