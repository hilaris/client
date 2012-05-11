javaaddpath('/home/km/Desktop/streamclient.jar');
client = ch.zhaw.hilaris.StreamClient();
client.connect('stud-kreismic-1', 9003);

javaImage = client.getImage();

% get image properties
H=javaImage.getHeight;
W=javaImage.getWidth;

% repackage as a 3D array (MATLAB image format)
B = uint8(zeros([H,W]));
pixelsData = uint8(javaImage.getData.getPixels(0,0,W,H,[]));
for i = 1 : H
    base = (i-1)*W + 1;
    B(i,1:W) = pixelsData(base:(base+W-1));
end

% display image
imshow(B);
client.disconnect();