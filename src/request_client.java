
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class request_client {

	public static void main(String[] args) {

		if(args.length!=3) {
			cantStartClient();
		}
		if(args[2].length()!=32)
			cantStartClient();
		int port=0;
		InetAddress serverAddress=null;
		try {
			port=new Integer(args[1]);
			if(port<10000 || port>65535)
				throw new Exception();
			serverAddress= InetAddress.getByName(args[0]);
		} catch (Exception e) {
			cantStartClient();
		}
		DatagramSocket datagramSocket=null;
		UDP udp=new UDP();
		try {
			datagramSocket=udp.createSocket(port);
		} catch (UnknownHostException ex) {
			cantStartClient();
		}

		//
		ClientClass client=new ClientClass(datagramSocket, port, serverAddress, args[2]);
		Thread clientThread=new Thread(client);
		clientThread.start();
		
		ClientPingerThread clientPinger=new ClientPingerThread(client);
		Thread clientPingerThread=new Thread(clientPinger);
		//clientPingerThread.start();
		
		ClientKillerThread clientKiller=new ClientKillerThread(client);
		Thread clientKillerThread=new Thread(clientKiller);
		clientKillerThread.start();
		
		System.out.println("\t\t\tWelcome to the Client.");			
		System.out.println(datagramSocket.getLocalPort());
		while(client.alive){
			Scanner scanner=new Scanner(System.in);
			System.out.println("*Press 1 to Close");
			int i=scanner.nextInt();
			if(i==1){
				client.die();
			}
		}

	}

	private static void cantStartClient() {
		System.out.println("Please Enter a Command of Form:");
		System.out.println("./request_client <server_hostname> <server_port> <hash>");
		System.out.println("where <port> is an integer between 10000 and 65535(inclusive).");
		System.exit(1);
	}
}
