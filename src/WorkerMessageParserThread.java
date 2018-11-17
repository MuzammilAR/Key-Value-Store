
import java.net.DatagramSocket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class WorkerMessageParserThread implements Runnable{
	public WorkerClass worker;
	public DatagramSocket datagramSocket;
	public Message message;
	public NodeInfo senderInfo;
	WorkerMessageParserThread(DatagramSocket datagramSocket, Message recievedMessage, WorkerClass workerClass, NodeInfo nodeInfo) {
		this.worker=workerClass; this.datagramSocket=datagramSocket;
		this.message=recievedMessage; this.senderInfo=nodeInfo;
	}

	@Override
	synchronized public void run() {
		worker.serverAlive=true;
		System.out.println(message.command);
		switch (message.command){
			case JOINED:
				worker.id=message.clientID;
			break;
			case PING:
				if (!worker.started){
					worker.sendMessageToServer(Command.DONE_NOT_FOUND,message.keyRangeStart);
				}
				else if (worker.started && !worker.done){
					worker.sendMessageToServer(Command.NOT_DONE,message.keyRangeStart);
				}
				else if (worker.done && !worker.found){					
					worker.sendMessageToServer(Command.DONE_NOT_FOUND,message.keyRangeStart);
					resetWorker();
				}
				else if (worker.done && worker.found){	
					worker.sendMessageToServer(Command.DONE_FOUND,worker.result);
					resetWorker();
				}
				break;
			case JOB:
				resetWorker();
				worker.started=true;
				worker.sendMessageToServer(Command.ACK_JOB,message.keyRangeStart);
				startJob();
				break;
			case CANCEL_JOB:
				resetWorker();
				break;
		}
	}

	private void startJob() {
		worker.hash=message.hash;
		String hash=message.hash;
		worker.keyEnd=message.keyRangeEnd;
		worker.keyStart=message.keyRangeStart;
		char[] c=message.keyRangeStart;
		int i=0,j=0,k=0,l=0,e=0;
		int arrayLen=Config.POSSIBLE_INPUT_CHAR.length;
		worker.result=null;
		worker.done=false;
		worker.found=false;
		while(i<arrayLen && worker.started){
			c[1]=Config.POSSIBLE_INPUT_CHAR[i];
			c[2]=Config.POSSIBLE_INPUT_CHAR[j];
			c[3]=Config.POSSIBLE_INPUT_CHAR[k];
			c[4]=Config.POSSIBLE_INPUT_CHAR[l];
			if(Hash.hash(String.valueOf(c)).equals(hash)){
				worker.result=c;
				worker.found=true;
				break;
			}
			l+=1;
			k+=l/arrayLen;
			j+=k/arrayLen;
			i+=j/arrayLen;
			j=j%arrayLen;
			k=k%arrayLen;
			l=l%arrayLen;
			if(i!=e){
				e=i;
				System.out.println(i);
			}
	}
		worker.done=true;
/*		if(worker.found)
			worker.sendMessageToServer(Command.DONE_FOUND, worker.result);
		else
			worker.sendMessageToServer(Command.DONE_NOT_FOUND, null);
*/	}

	private void resetWorker() {
		worker.keyEnd=null;worker.keyStart=null;worker.hash=null;worker.done=false;
		worker.result=null;
		worker.started=false;worker.found=false;
	}
}
