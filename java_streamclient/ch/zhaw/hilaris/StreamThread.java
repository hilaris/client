package ch.zhaw.hilaris;

import java.awt.Image;

public class StreamThread implements Runnable{
	private StreamClient client=null;
	private StreamPlayer player=null;
	
	private boolean cancel = false;
	
	public StreamThread(StreamClient client, StreamPlayer player) {
		this.client = client;
		this.player = player;
	}

	@Override
	public void run() {
		while(!this.cancel){
			Image i = client.getImage();
			player.updateImage(i);
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void cancel(){
		this.cancel=true;
	}

}
