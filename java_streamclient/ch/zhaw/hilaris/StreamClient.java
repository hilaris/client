package ch.zhaw.hilaris;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;


public class StreamClient {
	enum Perspective {DEFAULT, VERTICAL_MIRROR, HORIZONTAL_MIRROR, ROTATE};
	enum ImageType {GREY_FULLSIZE, GREY_HALFSIZE, BGR_FULLSIZE, BGR_HALFSIZE, RGB_FULLSIZE, RGB_HALFSIZE};
	
	private int imgWidth = 0;
	private int imgHeight = 0;
	private int msgSize = 0;
	private int imgType = 0;
	private ImageType type;
	
	private String host = "192.168.1.10";
	private int port = 9003;
	
	private InetAddress addr;
	private Socket sock;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	
	public StreamClient(){
		this.imgWidth = 752/2;
		this.imgHeight = 480/2;
		this.msgSize = this.imgWidth * this.imgHeight;
		this.imgType = BufferedImage.TYPE_3BYTE_BGR;
		this.type = ImageType.GREY_HALFSIZE;
	}
	
	public boolean connect(String host, int port){
		this.host = host;
		this.port = port;
		
		try {
			addr = Inet4Address.getByName(host);
			sock = new Socket(addr, port);
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return sock.isConnected();
	}
	
	public boolean disconnect(){
		try {
			this.in.close();
			this.out.close();
			this.sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this.sock.isClosed();		
	}
	
	public synchronized Image getImage(){
		byte[] bytes = new byte[msgSize];
				
		try
		{
			int totalRead = 0;
			
			while(totalRead < msgSize)
			{
				int bytesread = in.read(bytes, totalRead, this.msgSize - totalRead);
				if(bytesread < 0)
				{
					System.out.println("cant reach server anymore");
					System.exit(-1);
				}
				totalRead += bytesread;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		BufferedImage image = new BufferedImage(this.imgWidth, this.imgHeight, this.imgType);
		
		Raster raster = image.getData();
		DataBufferByte db = (DataBufferByte)raster.getDataBuffer();
		System.arraycopy(bytes, 0, db.getData(), 0, db.getData().length);
		image.setData(raster);
		
		return image;
	}
	
	public void setShutterWidth(int width){
		String command = "shutter:"+width;
		
		try {
			out.write(command.getBytes(), 0, command.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(command);
	}
	
	public void setPerspective(Perspective p){
		String command = "perspective:";
		switch (p){
		case DEFAULT:
			command += "default";
			break;
		case HORIZONTAL_MIRROR:
			command += "h_mirror";
			break;
		case VERTICAL_MIRROR:
			command += "v_mirror";
			break;
		case ROTATE:
			command += "rotate";
			break;
		default:
			command += "rotate";
			break;
		}
		
		try {
			out.write(command.getBytes(), 0, command.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(command);
	}
	
	public boolean isConnected(){
		if(this.sock!=null && this.sock.isConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public synchronized void setImageType(ImageType type, int width, int height)
	{
		this.type = type;
		switch(type)
		{
			case GREY_FULLSIZE:
				this.imgWidth = width;
				this.imgHeight = height;
				this.msgSize = this.imgWidth * this.imgHeight;
				this.imgType = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case GREY_HALFSIZE:
				this.imgWidth = width/2;
				this.imgHeight = height/2;
				this.msgSize = this.imgWidth * this.imgHeight;
				this.imgType = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case BGR_FULLSIZE:
				this.imgWidth = width;
				this.imgHeight = height;
				this.msgSize = this.imgWidth * this.imgHeight * 3;
				this.imgType = BufferedImage.TYPE_3BYTE_BGR;
				break;
			case BGR_HALFSIZE:
				this.imgWidth = width/2;
				this.imgHeight = height/2;
				this.msgSize = this.imgWidth * this.imgHeight * 3;
				this.imgType = BufferedImage.TYPE_3BYTE_BGR;
				break;
			default:
				break;
		}
	}
}
