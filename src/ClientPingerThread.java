
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
public class ClientPingerThread implements Runnable{
	private ClientClass client;

	public ClientPingerThread(ClientClass client) {
		this.client = client;
	}

	@Override
	synchronized public void run() {
		while(client.alive){
			client.sendMessageToServer(Command.PING);
			try {
				wait(Config.SERVER_PINGER_TIMEOUT);
			} catch (InterruptedException ex) {
				Logger.getLogger(ClientPingerThread.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
