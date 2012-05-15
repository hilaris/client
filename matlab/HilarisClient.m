classdef HilarisClient
    %HilarisClient a matlab wrapper for the java class StreamClient
    %   as matlab and java uses different image formats in this class image
    %   formats are converted. only color (BGR Halfsize) and grey (GREY
    %   Halfsize) are supported yet
    
    properties
        path = '';
        port = 0;
        client;
        connected = false;
    end
    
    methods
        function hc = HilarisClient(path, port)
           hc.path = path;
           hc.port = port;
           hc.client = ch.zhaw.hilaris.StreamClient;
        end
        
        function connect(obj)
          obj.connected = obj.client.connect(obj.path, obj.port);
        end
        
        function disconnect(obj)
            if(obj.client.disconnect())
                obj.connected = 0;
            else
                obj.connected = 1;
            end
        end
        
        function setShutterWidth(obj, width)
           obj.client.setShutterWidth(width); 
        end
        
        function setImageType(obj, type, width, height)
           if (strcmp(type,'grey'))
              obj.client.setImageType(javaMethod('valueOf', 'ch.zhaw.hilaris.StreamClient$ImageType', 'GREY_HALFSIZE'), width, height);
           elseif(strcmp(type,'color'))
               obj.client.setImageType(javaMethod('valueOf', 'ch.zhaw.hilaris.StreamClient$ImageType', 'BGR_HALFSIZE'), width, height);
           else
               obj.client.setImageType(javaMethod('valueOf', 'ch.zhaw.hilaris.StreamClient$ImageType', 'GREY_HALFSIZE'), width, height);
           end
        end
        
        function img = getImageData(obj)
            javaImg = obj.client.getImage();
            
            H=javaImg.getHeight;
            W=javaImg.getWidth;

            % repackage as an array (MATLAB image format)
            if (javaImg.getType == java.awt.image.BufferedImage.TYPE_BYTE_GRAY)
                img = uint8(zeros([H,W]));
                pixelsData = uint8(javaImg.getData.getPixels(0,0,W,H,[]));
                for i = 1 : H
                    base = (i-1)*W + 1;
                    img(i,1:W) = pixelsData(base:(base+W-1));
                end
            elseif (javaImg.getType == java.awt.image.BufferedImage.TYPE_3BYTE_BGR)
                img = uint8(zeros([H,W,3]));
                pixelsData = uint8(javaImg.getData.getPixels(0,0,W,H,[]));
                for i = 1 : H
                    base = (i-1)*W*3+1;
                    img(i,1:W,:) = deal(reshape(pixelsData(base:(base+3*W-1)),3,W)');
                end
            end
        end
    end
    
end

