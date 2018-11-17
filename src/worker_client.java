
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class worker_client {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if(args.length!=2) {
			cantStartWorker();
		}
		int port=0;
		InetAddress serverAddress=null;
		try {
			port=new Integer(args[1]);
			if(port<10000 || port>65535)
				throw new Exception();
			serverAddress= InetAddress.getByName(args[0]);
		} catch (Exception e) {
			cantStartWorker();
		}
		//create a worker client
				//create a server Socket
		DatagramSocket datagramSocket=null;
		UDP udp=new UDP();
		try {
			datagramSocket=udp.createSocket(port);
		} catch (UnknownHostException ex) {
			cantStartWorker();
		}

		WorkerClass worker=new WorkerClass(datagramSocket, port, serverAddress);
		Thread workerThread=new Thread(worker);
		workerThread.setDaemon(true);
		workerThread.start();
		
		WorkerKillerThread workerKiller=new WorkerKillerThread(worker);
		Thread workerKillerThread=new Thread(workerKiller);
		workerKillerThread.setDaemon(true);
		//workerKillerThread.start();//SORRY, THE WORKER KILLER THREAD IS STOPPED
		
		
		System.out.println("\t\t\tWelcome to the Worker.");			
		while(worker.alive){
			Scanner scanner=new Scanner(System.in);
			System.out.println("*Press 1 to Close");
			int i=scanner.nextInt();
			if(i==1){
				worker.dies();
			}
		}
	}

	private static void cantStartWorker() {
		System.out.println("Please Enter a Command of Form:");
		System.out.println("./worker_client <server_hostname> <server_port>");
		System.out.println("where <port> is an integer between 10000 and 65535(inclusive).");
		System.exit(1);
	}
}
