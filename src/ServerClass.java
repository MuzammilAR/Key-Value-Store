

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
public class ServerClass implements Runnable{
	public DatagramSocket datagramSocket;
	public ArrayList<Client> clients;
	public ArrayList<Worker> workers;
	public ArrayList<Message> jobsLeft;
	public ArrayList<CompletedJob> completedJobs;
	public ArrayList<char[]> possibleJobs;
	private boolean alive;
	private int clientID;
	private int port;
	public Thread jobAssignerThread;
	
	public ServerClass(DatagramSocket dSock) {
		datagramSocket=dSock;
		clients=new ArrayList<Client>();
		workers=new ArrayList<Worker>();
		jobsLeft=new ArrayList<Message>();
		completedJobs=new ArrayList<CompletedJob>();
		alive = true;
		clientID=2345;
		port=dSock.getLocalPort();
		possibleJobs=new ArrayList<char[]>();
		createJobs();
	}

	ServerClass(DatagramSocket datagramSocket, ServerDataClass serverDataClass) {
		this.datagramSocket=datagramSocket;
		this.clients=serverDataClass.clients;
		this.workers=serverDataClass.workers;
		this.jobsLeft=serverDataClass.jobsLeft;
		this.completedJobs=serverDataClass.completedJobs;
		this.alive=serverDataClass.alive;
		this.clientID=serverDataClass.clientID;
		this.port=serverDataClass.port;
		this.possibleJobs=serverDataClass.possibleJobs;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	public void createJobs(){
		char[] charArray=null;
		for(int i=0;i<Config.POSSIBLE_INPUT_CHAR.length;i++){
			charArray=new char[5];
			charArray[0]=Config.POSSIBLE_INPUT_CHAR[i];
			charArray[1]='a';
			charArray[2]='a';
			charArray[3]='a';
			charArray[4]='a';
			possibleJobs.add(charArray);
			//now end char
			charArray=new char[5];
			charArray[0]=Config.POSSIBLE_INPUT_CHAR[i];
			charArray[1]='9';
			charArray[2]='9';
			charArray[3]='9';
			charArray[4]='9';
			possibleJobs.add(charArray);
		
		}
	}
	
	public void die(){
		alive=false;
		datagramSocket.disconnect();
		datagramSocket.close();	
		System.out.println("Killing Server, Goodbye");
	}
	
	public boolean isAlive(){
		return alive;
	}
	public Worker getWorkerByID(long workerID){
		for(Worker worker:workers){
			if(worker.id==workerID)
				return worker;
		}
		return null;
	}
	
	public Client getClientByID(long clientID){
		for(Client worker:clients){
			if(worker.id==clientID)
				return  worker;
		}
		return null;
	}

	public int getPort(){
		return port;
	}
	
	public int getAndIncrementID(){
		clientID=(clientID+1573)%200000000;
		return clientID;
	}
	
	public synchronized void run(){
		UDP networkManager=new UDP();
		Message recievedMessage=null;
		ServerMessageParserThread msgParser=null;
		Thread t=null;
		NodeInfo nodeInfo=null;
		SocketAddress remoteAddress=null;
		while(this.alive){
			if(datagramSocket.isClosed())
				break;
			recievedMessage=networkManager.recieveMessageFrom(datagramSocket);
			if (recievedMessage==null)
				continue;
			if (recievedMessage.magic!=Config.MAGIC)
				continue;
			nodeInfo=networkManager.senderInfo;
			msgParser=new ServerMessageParserThread(datagramSocket, recievedMessage,this,nodeInfo);
			t=new Thread(msgParser);
			t.start();
		}
		System.out.println("Goodbye!");
		System.exit(0);
	}

	public int getRandomJob(){
		int index=(int)(Math.random()*jobsLeft.size());
		return index;
	}
	
	public void giveJob(Worker worker,int index) {
		if(jobsLeft.size()==0) return;
		Message message=jobsLeft.remove(index);
		message.clientID=worker.id;
		worker.job=message;
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(port);
		} catch (Exception ex) {
			Logger.getLogger(ServerClass.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		int i=0;
		udp.sendMessageTo(message, worker.info.getNodeLinkName(), worker.info.getNodePort(), socket);
		try{
			socket.disconnect();
			socket.close();
		}catch(Exception e){}
	}

	public void addJob(Client client) {
		//add client
		clients.add(client);
		//add jobs
		Message msg=null;
		for (int i=0; i<possibleJobs.size();i+=2){
			msg=new Message();
			msg.keyRangeEnd=possibleJobs.get(i+1);
			msg.keyRangeStart=possibleJobs.get(i);
			msg.clientID=client.id;
			msg.hash=client.job.hash;
			msg.command= Command.JOB;
			jobsLeft.add(msg);
		}
		//now assign jobs.
		for (Worker worker:workers){
			if (worker.job==null)
				giveJob(worker, getRandomJob());
		}
	}
}
