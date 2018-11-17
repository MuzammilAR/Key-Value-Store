/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class NodeClientThread extends Thread{
	Socket connectionSocket;
	public Node node;
	private DataOutputStream serverResponse;
	private BufferedReader clientRequest;

	public NodeClientThread(Socket connectionSocket, Node node) throws IOException {
		this.connectionSocket = connectionSocket;
		this.node = node;
		this.serverResponse=new DataOutputStream(this.connectionSocket.getOutputStream());
		this.clientRequest=new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
	}
		
	public synchronized void run(){
		try {
			String requestString=clientRequest.readLine();
			String[] requestArray=requestString.split(" , ");
			if(requestString.equals(Node.GET_NODE_INFORMATION)){
				serverResponse.writeBytes(this.node.getNodeInfo()+"\n");
			}
			else if(requestArray[0].equals(Node.STORE_FILE)){
				StoredFile storedFile=new StoredFile(requestArray[1], requestArray[2]);
				this.node.storedFiles.add(storedFile);
				serverResponse.writeBytes("OK\n"); 				
			}
			else if(requestArray[0].equals(Node.GET_NODE_FILES)){
				String myPredF=this.node.sendPredecessorFiles("200OK");
				serverResponse.writeBytes(myPredF+"\n"); 
			}
			else if(requestArray[0].equals(Node.SEND_NODE_FILES)){
				this.node.saveMyFiles(requestString);
				serverResponse.writeBytes("200OK"+"\n"); 
			}
			else if(requestArray[0].equals(Node.YOUR_PREDECESSOR_LEAVING)){
				this.node.saveMyFiles(requestString);
				this.node.predecessor=null;
				serverResponse.writeBytes("200OK"+"\n"); 
			}
			else if(requestArray[0].equals(Node.INFORM_NODE)){
				int port=0;
				if(requestArray[1].equals(Node.INFORM_YOU_ARE_SINGLE)){
					this.node.successor=null;this.node.predecessor=null;
					serverResponse.writeBytes("OK\n");
					return;
				}
				if(requestArray[1].equals(Node.INFORM_NOTIFY)){
					try {this.node.notify(requestString);} catch (Exception ex) {Logger.getLogger(NodeClientThread.class.getName()).log(Level.SEVERE, null, ex);}
					serverResponse.writeBytes("OK\n");
					return;
				}
				port= new Integer(requestArray[3]);
				if(requestArray[1].equals(Node.INFORM_THIS_IS_YOUR_PREDECESSOR)){
					this.node.predecessor=new NodeInfo(requestArray[2], port);
					if(this.node.successor==null)
						this.node.successor=new NodeInfo(requestArray[2], port);		
				}
				if(requestArray[1].equals(Node.INFORM_THIS_IS_YOUR_SUCCESSOR)){
					this.node.successor=new NodeInfo(requestArray[2], port);					
				}
				serverResponse.writeBytes("OK\n");
			}
			else if (requestArray[0].equals(Node.ASK)){
				if(requestArray[1].equals(Node.ASK_YOU_GOT_THIS_FILE)){
					String fileNameHash=requestArray[2];
					for(StoredFile storedFile:this.node.storedFiles){
						if(fileNameHash.equals(storedFile.hash)){
							serverResponse.writeBytes("YES\n");
							return;
						}
					}
					serverResponse.writeBytes("NO");
				}
				if(requestArray[1].equals(Node.ASK_WHOS_YOUR_PREDECESSOR)){
					if (this.node.predecessor==null){
						serverResponse.writeBytes("None ,   ,  "+"\n");
					}else{
						serverResponse.writeBytes(this.node.predecessor.getNodeInfoWithHash()+"\n");}
				}
				
			}
		} catch (IOException ex) {
			System.out.println("Sorry, The Connection to client was Lost while responding.");
		}
		close();
	}

	private void close() {
		try {			
			serverResponse.close();
			clientRequest.close();
			connectionSocket.close();
		} catch (IOException ex) {
			Logger.getLogger(NodeClientThread.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
