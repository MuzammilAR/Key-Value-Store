
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
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
public class server {

	/**
	 * @param args the command line arguments
	 */
	private static void cantStartServer(){
		System.out.println("Please Enter a Command of Form:");
		System.out.println("./server <port>");
		System.out.println("where <port> is an integer between 10000 and 65535(inclusive).");
		System.exit(1);		
	}
	
	public static void main(String[] args){
		DatagramSocket datagramSocket=null;
		ServerClass server=null;
		if(args.length==0){
			File f=new File(Config.SERVER_SERILIZABLE_FILE);
			if (!f.exists()){
				System.out.println("Sorry! No Backup found");
				cantStartServer();
			}
			ServerDataClass serverDataClass=null;
			System.out.println("Trying to Recover a Previous Server..");
			try(
				InputStream file = new FileInputStream(Config.SERVER_SERILIZABLE_FILE);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream (buffer);
			  ){
				serverDataClass= (ServerDataClass)input.readObject();
			 }catch(Exception e){
				 System.out.println("Sorry!");
				 cantStartServer();
			}
			try {
				datagramSocket=new DatagramSocket(serverDataClass.port, InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
				System.out.println("Server Running");
				System.out.print("Host: ");
				System.out.println(datagramSocket.getLocalAddress().getHostAddress());
				System.out.print("Port: ");
				System.out.println(datagramSocket.getLocalPort());
			} catch (Exception ex) {
				ex.printStackTrace();
				cantStartServer();
			}
			server=new ServerClass(datagramSocket,serverDataClass);

		}
		//////////////////////////////////////////////////////////////////////
		else{
			if(args.length!=1) {
				cantStartServer();
			}
			int port=0;
			try {
				port=new Integer(args[0]);
				if(port<10000 || port>65535)
					throw new Exception();
			} catch (Exception e) {
				cantStartServer();		
			}
			//create a server Socket
			UDP udp=new UDP();
			try {
				datagramSocket=udp.createSocket(port);
				System.out.println("Server Running");
				System.out.print("Host: ");
				System.out.println(datagramSocket.getLocalAddress().getHostAddress());
				System.out.print("Port: ");
				System.out.println(datagramSocket.getLocalPort());
			} catch (UnknownHostException ex) {
				cantStartServer();
			}
			//create a server, and start listening
			server=new ServerClass(datagramSocket);
		}

		Thread serverThread=new Thread(server);
		
		ServerPingerThread serverPinger=new ServerPingerThread(server);
		Thread serverPingerThread=new Thread(serverPinger);
		
		ServerJobAssignerThread serverJobAssigner=new ServerJobAssignerThread(server);
		Thread serverJobAssignerThread=new Thread(serverJobAssigner);
		
		ServerCleanerThread serverCleaner=new ServerCleanerThread(server);
		Thread serverCleanerThread=new Thread(serverCleaner);

		ServerSerializerThread serverSerializer=new ServerSerializerThread(server);
		Thread serverSerializerThread=new Thread(serverSerializer);
		/*
		serverThread.setDaemon(true);
		serverPingerThread.setDaemon(true);
		serverJobAssignerThread.setDaemon(true);
		serverCleanerThread.setDaemon(true);
		serverSerializerThread.setDaemon(true);
		*/
		server.jobAssignerThread=serverJobAssignerThread;

		serverThread.start();
		serverPingerThread.start();
		serverJobAssignerThread.start();
		serverCleanerThread.start();
		serverSerializerThread.start();
		
		System.out.println("\t\t\tWelcome to the Server.");			
		while(server.isAlive()){
			try{
			Scanner scanner=new Scanner(System.in);
			System.out.println("*Press 1 to Close");
			System.out.println("Press 2 to Crash the server.(Crashing forces the port to close and server crash)");
			int i=scanner.nextInt();
			if(i==1){
				server.die();
			}
			if(i==2){
				break;
			}
			}
			catch(Exception e){}
		}
		datagramSocket.disconnect();
		datagramSocket.close();
		//now run a server crash recovery thread
	}
}
