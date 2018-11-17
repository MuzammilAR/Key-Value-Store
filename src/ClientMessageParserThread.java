

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class ClientMessageParserThread implements Runnable{
	private ClientClass client;
	private Message recievedMessage;

	public ClientMessageParserThread(ClientClass client, Message recievedMessage) {
		this.client = client;
		this.recievedMessage = recievedMessage;
	}
		
	@Override
	public void run() {
		this.client.serverAlive=true;
		System.out.println(recievedMessage.command);
		switch (recievedMessage.command){
			case DONE_FOUND:
				System.out.println("Hash Found!");
				System.out.println(recievedMessage.keyRangeStart);
				client.alive=false;
				break;
			case DONE_NOT_FOUND:
				System.out.println("Sorry! No Hash Found!");
				client.alive=false;
				break;
			case NOT_DONE:
				break;
			case ACK_JOB:
				this.client.id=recievedMessage.clientID;
				break;
			case PING:
				client.sendMessageToServer(Command.PING);
				break;
		}
}
	
}
