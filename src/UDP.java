
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 * this code taken from here and then Modified
 * http://www.javaworld.com/article/2077539/learn-java/java-tip-40--object-transport-via-datagram-packets.html
 */
public class UDP {
	public NodeInfo senderInfo;//

	public UDP() {
		senderInfo=null;
	}
	
	
	public DatagramSocket createSocket(int port) throws UnknownHostException{
		String host=InetAddress.getLocalHost().getHostAddress();
		DatagramSocket socket=null;
		while(socket==null){
			try {
				socket=new DatagramSocket(port, InetAddress.getByName(host));
			} catch (Exception e) {
				socket=null;
				port=(port+1)%65536;
			}
		}
		return socket;
	}
	
	public void sendMessageTo(Serializable object, String host, int port,DatagramSocket dSock){
		try{
			ByteArrayOutputStream byteStream = new
			ByteArrayOutputStream(Config.UDP_PACKET_SIZE);
			ObjectOutputStream os = new ObjectOutputStream(new
							BufferedOutputStream(byteStream));
			os.flush();
			os.writeObject(object);
			os.flush();
			//retrieves byte array
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(
						sendBuf, sendBuf.length, InetAddress.getByName(host), port);
			int byteCount = packet.getLength();
			dSock.send(packet);
			os.close();
		}
		catch (UnknownHostException e)
		{
			System.err.println("Exception:  " + e);
			e.printStackTrace();    }
		catch (IOException e){ e.printStackTrace();
		}	
	}
	
	public Message recieveMessageFrom(DatagramSocket dSock){
		try{
			byte[] recvBuf = new byte[Config.UDP_PACKET_SIZE];
			DatagramPacket packet = new DatagramPacket(recvBuf,
												 recvBuf.length);
			dSock.receive(packet);
			senderInfo=new NodeInfo(packet.getAddress(),packet.getPort());
			int byteCount = packet.getLength();
			ByteArrayInputStream byteStream = new
								  ByteArrayInputStream(recvBuf);
			ObjectInputStream is = new
			ObjectInputStream(new BufferedInputStream(byteStream));
			Object o = is.readObject();
			is.close();
			return((Message)o);
		}
		catch (IOException e){
			System.err.println("Exception:  " + e);
			e.printStackTrace();
		}
		catch (ClassNotFoundException e){ 
			e.printStackTrace(); 
		}
		return(null);  
	}

}
