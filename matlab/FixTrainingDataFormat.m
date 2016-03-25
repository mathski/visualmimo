% Eric Wengrowski
% Created: 10/9/2015

function [ boostedGoodPoints, boostedBadPoints ] = FixTrainingDataFormat( k_ellipsoid_samples, k_ellipsoid_samples_binary_flicker_results, k_ellipsoid_samples_camera_BER, BER_threshold, BER_ONLY_FLAG)
%FixTrainingDataFormat  Sorts output data as good or bad based on flicker and
%BER. Used to retrain ellipsoids with Boosting.

    % Check length of inputs
    if( (length(k_ellipsoid_samples) ~= length(k_ellipsoid_samples_binary_flicker_results)) || (length(k_ellipsoid_samples) ~= length(k_ellipsoid_samples_camera_BER)) )
        disp('Error: Lengths of inputs are unequal.');
        return;
    end

    if(BER_ONLY_FLAG)
        k_ellipsoid_samples_binary_flicker_results = 0;
    end
    
    % Produce binary value for each 6D vector based on flicker and BER
    k_samples_good_points_mask = (k_ellipsoid_samples_camera_BER <= BER_threshold) & (~k_ellipsoid_samples_binary_flicker_results);
    
    boostedGoodPoints = [];
    boostedBadPoints = [];
    for i = 1:length(k_ellipsoid_samples)
        
        % Correct delta lengths
        delta_size = 5;
        x = k_ellipsoid_samples(i,4);
        y = k_ellipsoid_samples(i,5);
        z = k_ellipsoid_samples(i,6);
        individual_delta_length = delta_size / sqrt(x^2+y^2+z^2);
        k_ellipsoid_samples(i,4:6) = individual_delta_length*k_ellipsoid_samples(i,4:6);
        
        % Sort good and bad values
        if( k_samples_good_points_mask(i) )
            boostedGoodPoints = [boostedGoodPoints; k_ellipsoid_samples(i,:)];
        else
            boostedBadPoints = [boostedBadPoints; k_ellipsoid_samples(i,:)];
        end
    end

end