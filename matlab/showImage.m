javaaddpath('/home/km/Desktop/streamclient.jar');
client = ch.zhaw.hilaris.StreamClient();
client.connect('stud-kreismic-1', 9003);

thres = 10;
run = 1;

javaImage = client.getImage();

% get image properties
H=javaImage.getHeight;
W=javaImage.getWidth;

bg = uint8(zeros([H,W]));
pixelsData = uint8(javaImage.getData.getPixels(0,0,W,H,[]));
for i = 1 : H
    base = (i-1)*W + 1;
    bg(i,1:W) = pixelsData(base:(base+W-1));
end

while run
    javaImage = client.getImage();

    % get image properties
    H=javaImage.getHeight;
    W=javaImage.getWidth;

    % repackage as an array (MATLAB image format)
    B = uint8(zeros([H,W]));
    pixelsData = uint8(javaImage.getData.getPixels(0,0,W,H,[]));
    for i = 1 : H
        base = (i-1)*W + 1;
        B(i,1:W) = pixelsData(base:(base+W-1));
    end
    
    diff = uint8(zeros([H,W]));
    
    for j=1:H
        for k=1:W
            if(B(j,k)+thres>bg(j,k))
                bg(j,k) = bg(j,k)+1;
            elseif (B(j,k)-thres<bg(j,k))
                bg(j,k) = bg(j,k)-1;
            end
            

        end
    end
    
    diff = imabsdiff(bg, B);
    % display image
    %imshow(imsubtract(B, bg));
    fig = figure(1);
    subplot(2,2,1),imshow(B) 
    subplot(2,2,2),imshow(bg)
    subplot(2,2,3),imshow(diff) 
    sedisk = strel('disk', 1);
    subplot(2,2,4),imshow(imfill(imopen(im2bw(diff, 0.1),sedisk), 'holes')) 
    uicontrol('Style', 'pushbutton', 'String', 'stop','Position', [20 20 50 20], 'Callback', 'run=0;close(fig)');
    drawnow;
end
client.disconnect();