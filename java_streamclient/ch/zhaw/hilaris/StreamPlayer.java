package ch.zhaw.hilaris;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.zhaw.hilaris.StreamClient.ImageType;
import ch.zhaw.hilaris.StreamClient.Perspective;

public class StreamPlayer extends JFrame{

	private JPanel top;
	private JPanel middle;
	private JPanel bottom;
	
	private JTextField txtHost;
	private JTextField txtPort;
	
	private JButton btnStart;
	private JButton btnStop;
	
	private JComboBox cmbImageType;
	private JComboBox cmbPerspective;
	private JSlider sldShutterWidth;
	private JCheckBox chkAutoExposure;
	
	private String host = "192.168.1.10";
	private String port = "9003";
	
	private StreamThread thread = null;
	private StreamClient client = null;
	
	private Thread t = null;
	
	public static void main(String args[]){
		StreamPlayer player = new StreamPlayer();
	}
	
	public StreamPlayer(){		
		this.client = new StreamClient();
		
		initGUI();
	}
	
	public boolean initGUI(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			e2.printStackTrace();
		} catch (UnsupportedLookAndFeelException e2) {
			e2.printStackTrace();
		}
		
		this.top = new JPanel();
		this.middle = new JPanel();
		this.bottom = new JPanel();
		
		this.txtHost = new JTextField();
		this.txtPort = new JTextField();
		this.txtHost.setText(this.host);
		this.txtPort.setText(this.port);
		
		this.btnStart = new JButton("Start Stream");
		this.btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(btnStart.getText()=="Start Stream")
				{
					btnStart.setText("Stop Stream");
					txtHost.enable(false);
					txtPort.enable(false);
					startStream();
				}
				else
				{
					btnStart.setText("Start Stream");
					txtHost.enable(true);
					txtPort.enable(true);
					stopStream();
				}
				
			}
		});
		
		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		top.add(new JLabel("Hostname:"));
		top.add(this.txtHost);
		top.add(new JLabel("Port:"));
		top.add(this.txtPort);
		top.add(this.btnStart);
		
		this.sldShutterWidth = new JSlider(0, 100000, 2000);
		this.sldShutterWidth.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(client.isConnected())
				{
					client.setShutterWidth(sldShutterWidth.getValue());
				}
			}
		});
		
		String[] values = {"rotate", "v mirror", "h mirror", "default"};
		this.cmbPerspective = new JComboBox(values);
		this.cmbPerspective.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = cmbPerspective.getSelectedItem().toString();
				if(client.isConnected())
				{
					if(s=="h mirror")
					{
						client.setPerspective(Perspective.HORIZONTAL_MIRROR);
					}
					else if(s=="v mirror")
					{
						client.setPerspective(Perspective.VERTICAL_MIRROR);
					}
					else if(s=="rotate")
					{
						client.setPerspective(Perspective.ROTATE);
					}
					else if(s=="default")
					{
						client.setPerspective(Perspective.DEFAULT);
					}
				}
			}
		});
		
		String[] imgtypes = {"Greyscale Halfsize", "Greyscale", "BGR", "BGR Halfsize"};
		this.cmbImageType = new JComboBox(imgtypes);
		this.cmbImageType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = cmbImageType.getSelectedItem().toString();
				if(client.isConnected())
				{
					if(s=="Greyscale Halfsize")
					{
						client.setImageType(ImageType.GREY_HALFSIZE, 752, 480);
					}
					else if(s=="Greyscale")
					{
						client.setImageType(ImageType.GREY_FULLSIZE, 752, 480);
					}
					else if(s=="BGR")
					{
						client.setImageType(ImageType.BGR_FULLSIZE, 752, 480);
					}
					else if(s=="BGR Halfsize")
					{
						client.setImageType(ImageType.BGR_HALFSIZE, 752, 480);
					}
					middle.removeAll();
					middle.repaint();
				}
			}
		});
		
		this.chkAutoExposure = new JCheckBox("Autoexposure");
		this.chkAutoExposure.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(chkAutoExposure.isSelected())
				{
					sldShutterWidth.disable();
					client.setShutterWidth(-1);
				}
				else
				{
					sldShutterWidth.enable();
					client.setShutterWidth(sldShutterWidth.getValue());
				}
			}
		});
		
		bottom.setLayout(new FlowLayout());
		bottom.add(this.chkAutoExposure);
		bottom.add(this.sldShutterWidth);
		bottom.add(this.cmbPerspective);
		bottom.add(this.cmbImageType);
				
		this.setLayout(new BorderLayout());
		
		this.add(BorderLayout.NORTH, top);
		this.add(BorderLayout.CENTER, middle);
		this.add(BorderLayout.SOUTH, bottom);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setTitle("Hilaris Stream Client V2.0");
		this.setSize(800, 600);
		this.setVisible(true);
		
		return true;
	}
	
	private void startStream(){
		if(this.client.connect(this.txtHost.getText(), Integer.parseInt(this.txtPort.getText())))
		{
			this.client.setShutterWidth(this.sldShutterWidth.getValue());
			this.thread = new StreamThread(client, this);
			this.t = new Thread(this.thread);
			this.t.start();
		}
	}
	
	private void stopStream(){
		if(this.thread!=null)
		{
			this.thread.cancel();
			try {
				this.t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.client.disconnect();
			this.middle.removeAll();
			this.middle.repaint();
		}
	}
	
	public void updateImage(Image img){
		int w = this.middle.getWidth()/2;
		int h = this.middle.getHeight()/2;
		
		Graphics g = this.middle.getGraphics();
		g.drawImage(img, w-img.getWidth(null)/2, h-img.getHeight(null)/2, null);		
	}

}
