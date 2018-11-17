
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
public class WorkerKillerThread implements Runnable{
	private WorkerClass worker;

	public WorkerKillerThread(WorkerClass worker) {
		this.worker = worker;
	}

	@Override
	synchronized public void run() {
		while(worker.alive){
			try {
				wait(Config.SERVER_KILLER_TIMEOUT);
			} catch (InterruptedException ex) {
				Logger.getLogger(WorkerKillerThread.class.getName()).log(Level.SEVERE, null, ex);
			}
			if(!worker.serverAlive){
				worker.dies();
				System.out.println("Sorry! We could not connect to the server in some time");
				System.out.println("Try Again Later!");
			}
			worker.serverAlive=false;
		}
		System.exit(0);
	}	
}
