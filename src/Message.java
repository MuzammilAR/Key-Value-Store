
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */

public class Message implements Serializable{
	private static final long serialVersionUID = 1637238761L;
	public long magic;
	public long clientID;
	public Command command;
	public char[] keyRangeStart;
	public char[] keyRangeEnd;
	public String hash;
	//we'll keep the sender info in the application layer since we are using a 
	//udp socket and it is possible that a message sent from one host and when we see the remote socket, some other thread
	//may have changed it.
	public Message() {
		magic=Config.MAGIC;
		clientID=0;
		keyRangeEnd=new char[1];
		keyRangeStart=new char[1];
		hash="";
	}

	public Command getCommand() {
		return command;
	}

	void printEveryting() {
		System.out.print("\tHash: ");
		if(hash!=null)
			System.out.println(hash);
		else
			System.out.println("");
		System.out.print("\tKey Start: ");
		if(keyRangeStart!=null)
			System.out.println(keyRangeStart);
		else
			System.out.println("");
		System.out.print("\tKey End: ");
		if(keyRangeEnd!=null)
			System.out.println(keyRangeEnd);
		else
			System.out.println("");
	}

}
