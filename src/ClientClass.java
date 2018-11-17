
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
public class ClientClass implements Runnable{
	public DatagramSocket datagramSocket;
	public String hash;
	public NodeInfo serverInfo;
	public long id;
	public boolean alive;
	public boolean serverAlive;
	
	public ClientClass(DatagramSocket datagramSocket,int port, InetAddress serverAddress,String hash) {
		serverInfo=new NodeInfo(serverAddress, port);
		this.datagramSocket=datagramSocket;
		this.hash=hash;
		alive=true;
		serverAlive=true;
	}

	@Override
	synchronized public void run() {
		UDP networkManager=new UDP();
		Message recievedMessage=null;
		WorkerMessageParserThread msgParser=null;
		Thread t=null;
		NodeInfo nodeInfo=null;
		SocketAddress remoteAddress=null;
		try {
			//first join.
			datagramSocket.setSoTimeout(Config.WORKER_JOIN_WAIT_INTERVAL);
		} catch (SocketException ex) {
			Logger.getLogger(WorkerClass.class.getName()).log(Level.SEVERE, null, ex);
		}
		Message joinMessage=new Message();
		joinMessage.clientID=0;
		joinMessage.command= Command.HASH;
		joinMessage.hash=this.hash;
		int i=0;
		while(this.alive){
			i++;
			networkManager.sendMessageTo(joinMessage, serverInfo.getNodeLinkName(), serverInfo.getNodePort(), datagramSocket);
			recievedMessage=networkManager.recieveMessageFrom(datagramSocket);
			if(i>3) this.alive=false;
			if(recievedMessage==null)
				continue;
			if(i>3) this.alive=false;
			nodeInfo=networkManager.senderInfo;
			this.id=recievedMessage.clientID;
			parseMessage(recievedMessage);
			break;
		}
		if(!this.alive){
			System.out.println("Sorry, We couldn't connect in the given time.");
			System.exit(1);
		}
		try {
			datagramSocket.setSoTimeout(0);
		} catch (SocketException ex) {
			Logger.getLogger(WorkerClass.class.getName()).log(Level.SEVERE, null, ex);
		}
		while(this.alive){
			recievedMessage=networkManager.recieveMessageFrom(datagramSocket);
			if (recievedMessage==null)
				continue;
			if (recievedMessage.magic!=Config.MAGIC)
				continue;
			nodeInfo=networkManager.senderInfo;
			parseMessage(recievedMessage);
		}
		System.out.println("Goodbye!");
		System.exit(0);
	}

	void sendMessageToServer(Command command) {
		Message msg=new Message();
		msg.clientID=this.id;
		msg.command=command;
		msg.hash=this.hash;
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(serverInfo.getNodePort());
			udp.sendMessageTo(msg, serverInfo.getNodeLinkName(), serverInfo.getNodePort(), socket);
		} catch (UnknownHostException ex) {
			Logger.getLogger(WorkerClass.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void die(){
		alive=false;
		sendMessageToServer(Command.CANCEL_JOB);
	}
	
	private void parseMessage(Message recievedMessage) {
		ClientMessageParserThread clientMessageParserThread=new ClientMessageParserThread(this, recievedMessage);
		Thread t=new Thread(clientMessageParserThread);
		t.start();
	}
}
