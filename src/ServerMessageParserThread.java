
import java.net.DatagramSocket;
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
public class ServerMessageParserThread implements Runnable{
	private DatagramSocket datagramSocket;
	private Message message;
	private ServerClass server;
	private NodeInfo senderInfo;
	public ServerMessageParserThread(DatagramSocket datagramSocket, Message message, ServerClass server,NodeInfo senderInfo) {
		this.datagramSocket = datagramSocket;
		this.message = message;
		this.server=server;
		this.senderInfo=senderInfo;
	}

	@Override
	public synchronized void run(){
		Worker worker=null;
		Client client=null;
		System.out.println(message.command);
		System.out.print("Jobs Left Size: ");
		System.out.println(server.jobsLeft.size());
		switch (message.command){
			case PING:
				if(message.clientID==0)
					return;
				client=server.getClientByID(message.clientID);
				if(client==null)
					return;
				client.callExtraLife();
				char[] cc=null;
				for(CompletedJob completedJob:server.completedJobs){
					cc=completedJob.findMatch(client.job.hash);
					if(cc!=null){
						sendHashValue(cc, client.info);
						break;
					}
				}
				if(cc==null){
					sendNotDone(client);
				}
				break;
			case NOT_DONE:
				worker=server.getWorkerByID(message.clientID);
				worker.callExtraLife();
				break;
			case CANCEL_JOB:
				client=server.getClientByID(message.clientID);
				cancelJob(client);
				for (Worker worker1:server.workers){
					worker1.callExtraLife();
					if (worker1.job==null)
						continue;
					if(worker1.job.hash.equals(message.hash)){
						sendCancelJob(worker1);
						worker1.jobRecievedByWorker= false;
						worker1.job=null;
					}
				}

				break;
			case DONE_FOUND:
				worker=server.getWorkerByID(message.clientID);
				returnHashAndRemoveClient();
				if(worker!=null){
					if (worker.job==null)
						return;
					if (!worker.job.hash.equals(message.hash))
						return;
					worker.jobRecievedByWorker= false;
					worker.job=null;
					worker.callExtraLife();
				}
				//cancel all jobs related to me.
				for (Worker worker1:server.workers){
					if (worker1.job==null)
						continue;
					if(worker1.job.hash.equals(message.hash)){
						worker1.callExtraLife();
						sendCancelJob(worker1);
						worker1.jobRecievedByWorker= false;
						worker1.job=null;
					}
				}
				break;
			case DONE_NOT_FOUND:
				worker=server.getWorkerByID(message.clientID);
				System.out.println(worker.id);
				if (worker!=null){
					worker.callExtraLife();
					if (worker.job==null)
						return;
					if(worker.job.hash==null)
						return;
					if (!worker.job.hash.equals(message.hash))
						return;
					worker.jobRecievedByWorker= false;
					worker.callExtraLife();
					worker.job=null;
					server.giveJob(worker, server.getRandomJob());
				}
				else
					System.out.println("Hey! Problem bro. No worker with this ID:");
				break;
			case ACK_JOB:
				worker=server.getWorkerByID(message.clientID);
				if (worker!=null){
					worker.jobRecievedByWorker=true;
					worker.callExtraLife();
				}
				else
					System.out.println("Hey! Problem bro. No worker with this ID:");
				break;
			case HASH:
				//check in completed jobs.
				char[] hashValue=null;
				for (CompletedJob completedJob: server.completedJobs){
					hashValue=completedJob.findMatch(message.hash);
					if(hashValue!=null)
						break;
				}
				//if job already done then return that job.
				if (hashValue==null){
					addJob();
				}else{
					sendHashValue(hashValue,senderInfo);
				}
				break;
			case REQUEST_TO_JOIN:
				worker=new Worker(senderInfo);
				worker.id=server.getAndIncrementID();
				Worker worker2=checkWorkerExistance2(worker);
				if(worker2==null){//don't change this if. It is very IMP
					server.workers.add(worker);
					worker2=worker;
				}
				sendJoinedMessage(worker2);
				break;
		}
	}

	private boolean checkWorkerExistance(Worker worker) {
		for(Worker myWorker:server.workers){
			if (worker.info.equalsTo(myWorker.info))
				return false;
		}
		return true;
	}
	private Worker checkWorkerExistance2(Worker worker) {
		for(Worker myWorker:server.workers){
			if (worker.info.equalsTo(myWorker.info))
				return myWorker;
		}
		return null;
	}

	private boolean checkClientExistance(Client client) {
		for(Client myClient:server.clients){
			if (client.info.equalsTo(myClient.info)){
				if(client.job.hash.equals(myClient.job.hash))
					sendAckJob(myClient.id);
				//return false anyways. since we entertain only one job from a client.
				return false;
			}	
		}
		return true;
	}

	private void addJob() {		
		Client client=new Client(senderInfo, message, server.getAndIncrementID());
		if (checkClientExistance(client)){
			server.addJob(client);
			sendAckJob(client.id);
		}
	}

	private void sendHashValue(char [] hashValue, NodeInfo client){
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(server.getPort());
		} catch (Exception ex) {
			Logger.getLogger(ServerClass.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		Message msgSent=new Message();
		msgSent.command= Command.DONE_FOUND;
		msgSent.hash=message.hash;
		msgSent.keyRangeStart=hashValue;
		udp.sendMessageTo(msgSent, client.getNodeLinkName(), client.getNodePort(), socket);
		try{
			socket.disconnect();
			socket.close();
		}catch(Exception e){}
	}

	private void sendAckJob(int clID) {
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(server.getPort());
		} catch (Exception ex) {
			Logger.getLogger(ServerClass.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		Message msgSent=new Message();
		msgSent.clientID=clID;
		msgSent.command= Command.ACK_JOB;
		udp.sendMessageTo(msgSent, senderInfo.getNodeLinkName(), senderInfo.getNodePort(), socket);
		try{
			socket.disconnect();
			socket.close();
		}catch(Exception e){}
	}

	private void returnHashAndRemoveClient() {
		Client client=null;
		for(Client cl:server.clients){
			if(cl.job.hash.equals(message.hash)){
				client=cl;
				break;
			}
		}
		if(client==null)
			return;
		sendHashValue(message.keyRangeStart, client.info);
		
		cancelJob(client);
		//add a completed job.
		CompletedJob completedJob=new CompletedJob(message.hash, message.keyRangeStart);
		server.completedJobs.add(completedJob);
	}

	private void cancelJob(Client client){
		//remove client
		server.clients.remove(client);
		//remove all the jobs.
		ArrayList<Message> messagesToRemove=new ArrayList<Message>();
		for(Message msg:server.jobsLeft){
			if(msg.clientID==client.id)
				messagesToRemove.add(msg);
		}
		//remove those jobs.
		for(Message msg:messagesToRemove){
			server.jobsLeft.remove(msg);
		}
	}

	private void sendCancelJob(Worker worker1) {
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(server.getPort());
		} catch (Exception ex) {
			Logger.getLogger(ServerClass.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		Message msgSent=new Message();
		msgSent.clientID=worker1.id;
		msgSent.command= Command.CANCEL_JOB;
		udp.sendMessageTo(msgSent, worker1.info.getNodeLinkName(), worker1.info.getNodePort(), socket);
		try{
			socket.disconnect();
			socket.close();
		}catch(Exception e){}
	}

	private void sendJoinedMessage(Worker worker2) {
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(server.getPort());
		} catch (Exception ex) {
			Logger.getLogger(ServerClass.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		Message msgSent=new Message();
		msgSent.clientID=worker2.id;
		msgSent.command= Command.JOINED;
		udp.sendMessageTo(msgSent, worker2.info.getNodeLinkName(), worker2.info.getNodePort(), socket);
		try{
			socket.disconnect();
			socket.close();
		}catch(Exception e){}
	}

	private void sendNotDone(Client client) {
		UDP udp=new UDP();
		DatagramSocket socket=null;
		try {
			socket=udp.createSocket(server.getPort());
		} catch (Exception ex) {
			Logger.getLogger(ServerClass.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		Message msgSent=new Message();
		msgSent.clientID=client.id;
		msgSent.command= Command.NOT_DONE;
		udp.sendMessageTo(msgSent, client.info.getNodeLinkName(), client.info.getNodePort(), socket);
		try{
			socket.disconnect();
			socket.close();
		}catch(Exception e){}
	}
}
