
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
public class ServerCleanerThread implements Runnable{
	private ServerClass server;
	
	public ServerCleanerThread(ServerClass serverClass) {
		server=serverClass;
	}

	@Override
	synchronized public void run() {
		ArrayList<Worker> workersToKill=null;
		ArrayList<Client> clientsToKill=null;
		while (server.isAlive()){
			if(server.datagramSocket.isClosed())
				break;

			try{
			try {
				wait(Config.SERVER_KILLER_TIMEOUT);
			} catch (Exception e) {
			}
			workersToKill=new ArrayList<Worker>();
			clientsToKill=new ArrayList<Client>();
			System.out.println("Workers!");
			for (Worker worker:server.workers){
				if (worker.getLife()==0){
					server.jobsLeft.add(worker.job);
					workersToKill.add(worker);
				}
				worker.cleanLife();
				worker.printEverything();
			}
			for (Worker worker:workersToKill){
				System.out.print("Killing Worker: ");
				worker.printEverything();
				server.workers.remove(worker);
			}
			System.out.println("Clients!");
			for (Client client:server.clients){
				if (client.getLife()==0){
					clientsToKill.add(client);	
				}
				client.cleanLife();
				client.printEverything();
			}
			for (Client client:clientsToKill){
				cancelJob(client);
				for (Worker worker1:server.workers){
					if (worker1.job==null)
						continue;
					if(worker1.job.hash.equals(client.job.hash)){
						sendCancelJob(worker1);
						worker1.jobRecievedByWorker= false;
						worker1.job=null;
					}
				}
			}
			
			}
			catch(Exception e){e.printStackTrace();}
		}
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

}
