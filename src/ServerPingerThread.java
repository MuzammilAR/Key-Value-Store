
import java.net.DatagramSocket;
import java.net.UnknownHostException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class ServerPingerThread implements Runnable{
	private ServerClass server;
	
	public ServerPingerThread(ServerClass serverClass) {
		server=serverClass;
	}

	
	@Override
	synchronized public void run() {
		DatagramSocket datagramSocket=null;
		UDP udp=new UDP();
		try {
			datagramSocket=udp.createSocket(server.getPort());
		} catch (UnknownHostException ex) {
			System.exit(1);
		}
		Message message=null;
		while (server.isAlive()){
			if(server.datagramSocket.isClosed())
				break;
			for (Worker worker:server.workers){
				message=new Message();
				message.command= Command.PING;
				message.clientID=worker.id;
				udp.sendMessageTo(message, worker.info.getNodeLinkName(), worker.info.getNodePort(), datagramSocket);
			}
			for (Client client:server.clients){
				message=new Message();
				message.command= Command.PING;
				message.clientID=client.id;
				udp.sendMessageTo(message, client.info.getNodeLinkName(), client.info.getNodePort(), datagramSocket);
			}

			try {
				wait(Config.SERVER_PINGER_TIMEOUT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try{
			datagramSocket.disconnect();
			datagramSocket.close();
		}catch(Exception e){}

	}

}
