import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JFrame;


public class StreamImage extends JFrame{
	
	private int imgWidth = 752/2;
	private int imgHeight = 480/2;
	private int msgSize = imgWidth * imgHeight * 3;
	private int imgType = BufferedImage.TYPE_3BYTE_BGR;
	
	public StreamImage()
	{		
		this.setVisible(true);
		this.setSize(this.imgWidth, this.imgHeight);
		this.setTitle("leanXStreamClient");
		//this.setResizable(false);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		});
	}
	
	public static void main(String[] args)
	{
		StreamImage stream = new StreamImage();
		String ip = "192.168.1.10";
		if(args.length >0){
			
			ip = args[0];
		}
		
		stream.start(ip);

	}
	
	public void start(String host){
		Calendar cal = Calendar.getInstance();
		long start = cal.getTimeInMillis();
		
		int msgSize = this.msgSize;
		char[] buffer = new char[msgSize];
		byte[] bytes = new byte[msgSize];
		InetAddress addr;
		Socket sock;
		DataInputStream in = null;
		try {
			addr = Inet4Address.getByName(host);
			sock = new Socket(addr, 12345);
			in = new DataInputStream(sock.getInputStream());
			
		} catch (UnknownHostException e1) {
			System.exit(0);
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int count = 0;
		
		while(true)
		{
			try
			{
				int totalRead = 0;
				
				while(totalRead < msgSize)
				{
					int bytesread = in.read(bytes, totalRead, msgSize - totalRead);
					if(bytesread < 0)
					{
						System.out.println("cant reach server anymore");
						System.exit(-1);
					}
					totalRead += bytesread;
					//System.out.println(totalRead);
				}
				long now = new Date().getTime();
				
				System.out.println("recieved image nr "+(++count)+" ("+(now-start)/1000.0+")");
								
				BufferedImage image = new BufferedImage(this.imgWidth, this.imgHeight, this.imgType);

				Raster raster = image.getData();
		        DataBufferByte db = (DataBufferByte)raster.getDataBuffer();
		        System.arraycopy(bytes, 0, db.getData(), 0, db.getData().length);
		        image.setData(raster);
						
				
				//ImageIO.write(image, "BMP", new File("filename.bmp"));
				Graphics g = this.getGraphics();
				
				g.drawImage(image, 0, 0, null);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
