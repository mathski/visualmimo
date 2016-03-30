% Train Differential Metamers Classifier
% Eric Wengrowski
% Created 1.26.2016
% Last Edited 1.26.2016

function [] = trainDiffMetamers(varargin)

%k = varargin{1}; % number of clusters
%BER_threshold = varargin{2};
%show_kde = varargin{3};

if size(varargin) < 1
    k = 30;
else
    k = varargin{1};
end
if size(varargin) < 2
    BER_threshold = 0;
    BER_ONLY_FLAG = true; %ignore binary flicker results
else
    BER_threshold = varargin{2};
end
if size(varargin) < 3
    show_kde = true;
else
    show_kde = varargin{3};
end


%% Load Training Data
load Vgood.mat
load Vbad.mat
load k_50_ellipsoid_boost_100_samples.mat
load k_50_ellipsoid_boost_100_samples_binary_flicker.mat
load k_50_ellipsoid_boost_100_samples_camera_BER.mat
[boostedGoodPoints1,boostedBadPoints1] = FixTrainingDataFormat(k_50_ellipsoid_boost_100_samples', k_50_ellipsoid_boost_100_samples_binary_flicker', k_50_ellipsoid_boost_100_samples_camera_BER', BER_threshold,BER_ONLY_FLAG);
load k_50_means_ellipsoids_unpopulated_100_samples.mat
load k_50_means_ellipsoids_unpopulated_100_samples_binary_flicker_re.mat
load k_50_means_ellipsoids_unpopulated_100_samples_camera_BER.mat
[boostedGoodPoints2,boostedBadPoints2] = FixTrainingDataFormat(k_50_means_ellipsoids_unpopulated_100_samples', k_50_means_ellipsoids_unpopulated_100_samples_binary_flicker_re', k_50_means_ellipsoids_unpopulated_100_samples_camera_BER', BER_threshold,BER_ONLY_FLAG);
load k_50_ellipsoid_1000_samples.mat
load k_50_ellipsoid_1000_samples_binary_flicker_results.mat
load k_50_ellipsoid_1000_samples_camera_results.mat
[boostedGoodPoints3,boostedBadPoints3] = FixTrainingDataFormat(k_50_ellipsoid_1000_samples', k_50_ellipsoid_1000_samples_binary_flicker_results', k_50_ellipsoid_1000_samples_camera_results', BER_threshold,BER_ONLY_FLAG);
    
boostedGoodPoints = [boostedGoodPoints1; boostedGoodPoints2; boostedGoodPoints3];
boostedBadPoints = [boostedBadPoints1; boostedBadPoints2; boostedBadPoints3];
    
Vgood_all = [Vgood(:,1:6); boostedGoodPoints];
Vbad_all  = [Vbad(:,1:6); boostedBadPoints];


%% Normalize color shift delta magnitude
delta_size = 1;
for i = 1:length(Vgood_all)
    x = Vgood_all(i,4);
    y = Vgood_all(i,5);
    z = Vgood_all(i,6);
    individual_delta_length = delta_size ./ sqrt(x^2+y^2+z^2);
    Vgood_all(i,4:6) = individual_delta_length.*[x,y,z];
end
for i = 1:length(Vbad_all)
    x = Vbad_all(i,4);
    y = Vbad_all(i,5);
    z = Vbad_all(i,6);
    individual_delta_length = delta_size ./ sqrt(x^2+y^2+z^2);
    Vbad_all(i,4:6) = individual_delta_length.*[x,y,z];
end

% Normalize color values
Vgood_all = Vgood_all/255;
Vbad_all = Vbad_all/255;

%% Play with different color spaces
% reshape data from list form to image form
for i = 1:length(Vgood_all)
    for rgb = 1:3
        Vgood_converted(i,1,rgb) = double(Vgood_all(i,rgb));
        Vgood_delta(i,1,rgb) = double(Vgood_all(i,rgb) + Vgood_all(i,3+rgb));
        %Vgood_delta(i,1,rgb) = double(Vgood_all(i,3+rgb));
    end
end
 for i = 1:length(Vbad_all)
    for rgb = 1:3
        Vbad_converted(i,1,rgb) = double(Vbad_all(i,rgb));
        Vbad_delta(i,1,rgb) = double(Vbad_all(i,rgb) + Vbad_all(i,3+rgb));
        %Vbad_delta(i,1,rgb) = double(Vbad_all(i,3+rgb));
    end
end

% TRAINING DATA IS RGB --> CONVERT TO LAB 
    Vgood_converted = rgb2lab(Vgood_converted);
    Vgood_delta = rgb2lab(Vgood_delta);
    Vbad_converted = rgb2lab(Vbad_converted);
    Vbad_delta = rgb2lab(Vbad_delta);

% Reshape data back into list form
for i = 1:length(Vgood_all)
    for rgb = 1:3
        Vgood_all(i,rgb) = double(Vgood_converted(i,1,rgb));
        Vgood_all(i,3+rgb) = double(Vgood_delta(i,1,rgb));
    end
end
for i = 1:length(Vbad_all)
    for rgb = 1:3
        Vbad_all(i,rgb) = double(Vbad_converted(i,1,rgb));
        Vbad_all(i,3+rgb) = double(Vbad_delta(i,1,rgb));
    end
end

%% Find Kernel Density Estimate of data
if(show_kde)
    % KDE of base colors
    figure
    bandwidth_base = 2.5;
    bandwidth_delta = 0.01;
    [f,xi] = ksdensity(Vgood_all(:,1),'bandwidth', bandwidth_base);
    subplot(2,3,1)
    plot(xi,f)
    [f,xi] = ksdensity(Vgood_all(:,2),'bandwidth', bandwidth_base);
    subplot(2,3,2)
    plot(xi,f)
    [f,xi] = ksdensity(Vgood_all(:,3),'bandwidth', bandwidth_base);
    subplot(2,3,3)
    plot(xi,f)
    % KDE of delta colors
    [f,xi] = ksdensity(Vgood_all(:,4)-Vgood_all(:,1),'bandwidth', bandwidth_delta);
    subplot(2,3,4)
    plot(xi,f)
    [f,xi] = ksdensity(Vgood_all(:,5)-Vgood_all(:,2),'bandwidth', bandwidth_delta);
    subplot(2,3,5)
    plot(xi,f)
    [f,xi] = ksdensity(Vgood_all(:,6)-Vgood_all(:,3),'bandwidth', bandwidth_delta);
    subplot(2,3,6)
    plot(xi,f)
    %title(['KDE with bandwidth = ' num2str(bandwidth)])
end

%% Cluster the training data
%{
% K-means
tic
kmeans_tic = tic;
[idx,Centroids] = kmeans(Vgood_all(:,1:6),k);
kmeans_time = toc(kmeans_tic)
%
% K-medoids
tic
kmedoids_tic = tic;
[idx,Centroids] = kmedoids(Vgood_all(:,1:6),k);
kmedoids_time = toc(kmedoids_tic)
%
% Heirarchical Clustering
tic
hc_tic = tic;
tree = linkage(Vgood_all(:,1:6),'average');
figure, dendrogram(tree,200);
idx = cluster(tree,'maxclust',k);
hc_time = toc(hc_tic)
%
% Gaussian Mixture Models
%GMModel = fitgmdist(Vgood_all(:,1:6),k,'Replicates',5,'Options',statset('Display','iter','MaxIter',1500));
tic
gmm_tic = tic;
[MEANS, COVARIANCES, PRIORS, LL, POSTERIORS] = vl_gmm(Vgood_all(:,1:6)',k,'verbose','MaxNumIterations',1500,'NumRepetitions',10);
[~,ind] = max(POSTERIORS);
[idx,~] = ind2sub(size(POSTERIORS),ind);
idx = idx';
gmm_time = toc(gmm_tic)
%
% Spectral Clustering
tic
spectral_tic = tic;
sigma = 5;
idx = spcl(Vgood_all(:,1:6),k,sigma,'unormalized','kmean');
spectral_time = toc(spectral_tic)
%}

%% Train Binary Classifier


