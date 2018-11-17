
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
public class WorkerClass implements Runnable{
	public DatagramSocket datagramSocket;
	public char [] keyStart;
	public char [] keyEnd;
	public String hash;
	public boolean done;
	public char [] result;
	public boolean alive;
	public boolean started;
	public NodeInfo serverInfo;
	public long id;
	boolean found;
	public boolean serverAlive;

	public WorkerClass(DatagramSocket datagramSocket,int port, InetAddress serverAddress) {
		serverInfo=new NodeInfo(serverAddress, port);
		this.datagramSocket=datagramSocket;
		keyEnd=null;keyStart=null;hash=null;done=false;result=null;alive=true;id=0;
		started=false;found=false;
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
		joinMessage.command= Command.REQUEST_TO_JOIN;
		int i=0;
		while(this.alive){
			i++;
			networkManager.sendMessageTo(joinMessage, serverInfo.getNodeLinkName(), serverInfo.getNodePort(), datagramSocket);
			recievedMessage=networkManager.recieveMessageFrom(datagramSocket);
			if(i>3) this.alive=false;
			if(recievedMessage==null)
				continue;
			nodeInfo=networkManager.senderInfo;
			this.id=recievedMessage.clientID;
			msgParser=new WorkerMessageParserThread(datagramSocket, recievedMessage,this,nodeInfo);
			t=new Thread(msgParser);
			t.start();
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
			msgParser=new WorkerMessageParserThread(datagramSocket, recievedMessage,this,nodeInfo);
			t=new Thread(msgParser);
			t.start();
		}
		System.out.println("Goodbye!");
		System.exit(0);
	}

	void sendMessageToServer(Command command,char [] returnValue) {
		Message msg=new Message();
		msg.clientID=this.id;
		msg.command=command;
		msg.hash=this.hash;
		msg.keyRangeStart=returnValue;
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(serverInfo.getNodePort());
			udp.sendMessageTo(msg, serverInfo.getNodeLinkName(), serverInfo.getNodePort(), socket);
		} catch (UnknownHostException ex) {
			Logger.getLogger(WorkerClass.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	void dies(){
		this.alive=false;
	}
}
