javaaddpath('/home/km/Desktop/streamclient.jar');
client = HilarisClient('stud-kreismic-1', 9003);
client.connect();

thres = 10;
run = 1;


bg = client.getImageData();

while run
    frame = client.getImageData();

    % get image properties
    [H,W] = size(frame);
    
    diff = uint8(zeros([H,W]));
    
    for j=1:H
        for k=1:W
            if(frame(j,k)+thres>bg(j,k))
                bg(j,k) = bg(j,k)+1;
            elseif (frame(j,k)-thres<bg(j,k))
                bg(j,k) = bg(j,k)-1;
            end
            

        end
    end
    
    diff = imabsdiff(bg, frame);
    
    %display the images
    fig = figure(1);
    subplot(2,2,1),imshow(frame) 
    subplot(2,2,2),imshow(bg)
    subplot(2,2,3),imshow(diff) 
    sedisk = strel('disk', 1);
    subplot(2,2,4),imshow(imfill(imopen(im2bw(diff, 0.1),sedisk), 'holes')) 
    uicontrol('Style', 'pushbutton', 'String', 'stop','Position', [20 20 50 20], 'Callback', 'run=0;close(fig)');
    drawnow;
end
client.disconnect();