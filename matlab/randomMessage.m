function [message] = randomMessage(length, width)

% ensure equal number on and off
on_left = length * width / 2;
off_left = length * width / 2;

message = [];
for i=1:length * width 
    randed = round(rand(1));
    if randed == 1
        if on_left > 0
            on_left = on_left - 1;
        else
            off_left = off_left - 1;
            randed = 0;
        end
    else
        if off_left > 0
            off_left = off_left - 1;
        else
            on_left = on_left - 1;
            randed = 1;
        end
    end
    message = [message, randed];
end

end