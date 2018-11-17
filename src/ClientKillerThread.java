
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class ClientKillerThread implements Runnable{
	private ClientClass client;

	public ClientKillerThread(ClientClass client) {
		this.client = client;
	}

	@Override
	synchronized public void run() {
		while(client.alive){
			try {//client waits ten minutes before dying.
				wait(Config.CLIENT_KILLER_TIMEOUT);
			} catch (InterruptedException ex) {
				Logger.getLogger(ClientKillerThread.class.getName()).log(Level.SEVERE, null, ex);
			}
			if(!client.serverAlive){
				client.alive=false;
				System.out.println("Sorry! We could not connect to the server in some time");
				System.out.println("Try Again Later!");
			}
			client.serverAlive=false;
		}
		System.exit(0);

	}	
}
