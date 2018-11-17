
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
public class ServerJobAssignerThread implements Runnable{
	private ServerClass server;
	
	public ServerJobAssignerThread(ServerClass serverClass) {
		server=serverClass;
	}

	
	@Override
	synchronized public void run() {
		while (server.isAlive()){
			if(server.datagramSocket.isClosed())
				break;
			try{
			for (Worker worker:server.workers){
				if (worker.job!=null)
					continue;
				server.giveJob(worker, server.getRandomJob());
			}
			try {
				wait(Config.SERVER_JOB_ASSIGNER_TIMEOUT);
			} catch (Exception e) {
			}
			}catch(Exception e){e.printStackTrace();}
		}
	}

}
