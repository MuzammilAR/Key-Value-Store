/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class Node {
	public NodeInfo successor;
	public NodeInfo predecessor;
	public NodeInfo nodeInfo;
	public ArrayList<StoredFile> storedFiles;
	private String fileDirectory;
	boolean isAlive;
	public static String GET_NODE_INFORMATION="GetNodeInfoWithHash";
	public static String GET_NODE_FILES="GetNodeFIles";
	public static String SEND_NODE_FILES="SendNodeFiles";
	public static String INFORM_NODE="Inform";
	public static String INFORM_THIS_IS_YOUR_PREDECESSOR="ThisIsYourPredecessor";
	public static String INFORM_THIS_IS_YOUR_SUCCESSOR="ThisIsYourSuccessor";
	public static String ASK="Ask";
	public static String ASK_WHOS_YOUR_PREDECESSOR="Who'sYourPredecessor";
	public static String INFORM_NOTIFY="Notify";
	public static String YOUR_SUCCESSOR_LEAVING="YourSuccessorLeaving";
	public static String YOUR_PREDECESSOR_LEAVING="YourPredecessorLeaving";
	public static String LEAVE="Leave";
	public static String INFORM_YOU_ARE_SINGLE="YouAreTheOnlyNodeInDHT";
	public static String ASK_YOU_GOT_THIS_FILE="AskYouGotThisFile";
	public static String STORE_FILE="STOREFILE";
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); //To change body of generated methods, choose Tools | Templates.
	}
	
	public Node(NodeInfo successor, NodeInfo predecessor, NodeInfo nodeInfo, ArrayList<StoredFile> storedFiles, String fileDirectory, boolean isAlive, ServerSocket nodeServerSocket, NodeServerThread nodeServerThread, NodeStablizerThread nodeStablizerThread) {
		this.successor = successor;
		this.predecessor = predecessor;
		this.nodeInfo = nodeInfo;
		this.storedFiles = storedFiles;
		this.fileDirectory = fileDirectory;
		this.isAlive = isAlive;
		this.nodeServerSocket = nodeServerSocket;
		this.nodeServerThread = nodeServerThread;
		this.nodeStablizerThread = nodeStablizerThread;
	}
	//some other variables related to server socket. No need to track them but do it for safety
	private ServerSocket nodeServerSocket;
	private NodeServerThread nodeServerThread;
	private	NodeStablizerThread nodeStablizerThread;
		
	public Node(NodeInfo successor, NodeInfo predecessor, NodeInfo myInfo) {
		this.successor = successor;
		this.predecessor = predecessor;
		this.nodeInfo = myInfo;
		this.storedFiles=new ArrayList<StoredFile>();
		this.fileDirectory=this.nodeInfo.getNodeInfo();
		nodeServerSocket=null;
		isAlive=false;
	}

	public Node() {
		this.successor = null;
		this.predecessor = null;
		this.nodeInfo = null;
		this.storedFiles=new ArrayList<StoredFile>();
		this.fileDirectory="";
		nodeServerSocket=null;
		isAlive=false;
	}

	public Node(InetAddress ip,int port) {
		this.successor = null;
		this.predecessor = null;
		this.nodeInfo=new NodeInfo(ip, port);
		this.storedFiles=new ArrayList<StoredFile>();
		this.fileDirectory=this.nodeInfo.getNodeInfo();
		nodeServerSocket=null;
		isAlive=false;
	}

	public void create(int port) throws IOException{
		//make a server socket!.
		System.out.println("Creating Server Socket on port: "+String.valueOf(port));
		nodeServerSocket = new ServerSocket(port, 10, InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));;
		InetAddress ip=nodeServerSocket.getInetAddress();
		System.out.println(ip.getHostAddress());
		this.nodeInfo=new NodeInfo(ip, port);
		this.fileDirectory=this.nodeInfo.getNodeInfo();
		System.out.println("Server Socket Created.");
		//create directory
		//make a child thread and pass that server socket
		this.isAlive=true;
		nodeServerThread = new NodeServerThread(nodeServerSocket, this);
		nodeServerThread.node=this;
		nodeServerThread.start();
		//start a stablizer thread
		nodeStablizerThread=new NodeStablizerThread(this);
		nodeStablizerThread.node=this;
		nodeStablizerThread.start();
	}

	private boolean createDirectory() {
		File file=new File(this.fileDirectory);
		if (file.exists()){
			System.out.println("Directory Already Exists.");
			return true;
		}
		if(file.mkdir()){
			System.out.println("Directory Created.");
			return true;
		}
		return false;
	}
	
	public String getNodeInfo(){
		//format
		//Node IP , Node 
		String string="";
		string+=nodeInfo.getNodeInfoWithHash();
		if (successor==null){
			string+=" , None ,   ,  ";}
		else{
			string+=" , ";
			string+=successor.getNodeInfoWithHash();}
		
		if (predecessor==null){
			string+=" , None ,   ,  ";}
		else{
			string+=" , ";
			string+=predecessor.getNodeInfoWithHash();}
		return string;
	}
	private String getResponse(String host,int port, String string) throws UnknownHostException, IOException{
		BufferedReader serverResponse=null;
		DataOutputStream clientRequest=null;
		Socket clientSocket=null;
		String response="";
		clientSocket = new Socket(host, port);
			if (!clientSocket.isConnected()){
				System.out.println("Sorry No Connection could be made to the given host");
				System.out.println("We can't join.");
				throw new UnknownHostException();
			}
			serverResponse=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			clientRequest=new DataOutputStream(clientSocket.getOutputStream());
			clientRequest.writeBytes(string+"\n");
			response=serverResponse.readLine();
			//close the connection
			try{
				clientRequest.close();
				serverResponse.close();
				clientSocket.close();
			}catch(Exception e){}
		return response;
	}

	public void join(int port, String dhtLinkName,int dhtLinkPort) throws Exception{
		create(port);
		nodeStablizerThread=null;
		//complete it.
		findSuccessor(dhtLinkName, dhtLinkPort);
		nodeStablizerThread=new NodeStablizerThread(this);
		nodeStablizerThread.node=this;
		nodeStablizerThread.start();
	}

	private void findSuccessor(String dhtLinkName,int dhtLinkPort) throws Exception {
		System.out.println("Finding Successor");
		String response="";
		String[] responseArray;
		String ip;
		while(this.successor==null){
			System.out.println("Asking "+dhtLinkName+" "+ String.valueOf(dhtLinkPort));
			response=getResponse(dhtLinkName, dhtLinkPort, GET_NODE_INFORMATION);
			System.out.println(response);
			System.out.println("My hash;"+this.nodeInfo.getNodeHash());
			responseArray=response.split(" , ");
			ip="";int port=0;
			//successor or predecessor is none
			if(responseArray[3].equals("None")){
			System.out.println("Only One Node in the DHT.");
				ip=responseArray[0];
				port=new Integer(responseArray[1]);
				this.successor=new NodeInfo(ip, port);
				ip=responseArray[0];
				port=new Integer(responseArray[1]);
				this.predecessor=new NodeInfo(ip, port);
				continue;
			}//if hash of current node is larger than pred and successor, then  it is largest node.
			if(responseArray[2].compareTo(responseArray[5])>0 && responseArray[2].compareTo(responseArray[8])>0){
				System.out.println("Looky Here! We got the largest node.");
				System.out.println(responseArray[8]);
				System.out.println(responseArray[2]);
				System.out.println(responseArray[5]);
				if(this.nodeInfo.getNodeHash().compareTo(responseArray[2])>0 || this.nodeInfo.getNodeHash().compareTo(responseArray[5])<0){
					System.out.println("My hash;"+this.nodeInfo.getNodeHash());
					ip=responseArray[3];
					port=new Integer(responseArray[4]);
					this.successor=new NodeInfo(ip, port);
					ip=responseArray[0];
					port=new Integer(responseArray[1]);
					this.predecessor=new NodeInfo(ip, port);
					continue;
				}
				//now if it is between 
				System.out.println("Moving on from Largest Node");
				dhtLinkName=responseArray[3];
				dhtLinkPort= new Integer(responseArray[4]);
				continue;
			}
			//normal
			System.out.println("A Regular Node.");
			System.out.println(responseArray[8]);
			System.out.println(responseArray[2]);
			System.out.println(responseArray[5]);
			if(this.nodeInfo.getNodeHash().compareTo(responseArray[2])>0 && this.nodeInfo.getNodeHash().compareTo(responseArray[5])<0){
				System.out.println("My hash;"+this.nodeInfo.getNodeHash());
				ip=responseArray[3];
				port=new Integer(responseArray[4]);
				this.successor=new NodeInfo(ip, port);
				ip=responseArray[0];
				port=new Integer(responseArray[1]);
				this.predecessor=new NodeInfo(ip, port);
				continue;
			}
			//now if it is between 
			System.out.println("Moving on from Regular Node");
			dhtLinkName=responseArray[3];
			dhtLinkPort= new Integer(responseArray[4]);				
			continue;
		}
		this.informPredecessorOfYourself();
		this.informSucessorOfYourself();
		this.getFilesFromSuccessor();
	}
	
	private void getFilesFromSuccessor() throws Exception {
		String response=getResponse(this.successor.getNodeLinkName(), 
				this.successor.getNodePort(), 
				GET_NODE_FILES+" , "+this.getNodeInfo());
		System.out.println(response);
		saveMyFiles(response);
	}

	public void saveMyFiles(String response){
		String[] responseArray=response.split(" , ");
		for (int i = 1; i < responseArray.length; i+=2){
			storedFiles.add(new StoredFile(responseArray[i], responseArray[i+1]));
		}
	}
	
	private void informSucessorOfYourself() throws Exception {
		this.informOfYourself(successor,INFORM_THIS_IS_YOUR_PREDECESSOR);
	}
	private void informPredecessorOfYourself() throws Exception {
		this.informOfYourself(predecessor,INFORM_THIS_IS_YOUR_SUCCESSOR);
	}

	private void informOfYourself(NodeInfo info, String infoString) throws Exception{
		String response=getResponse(info.getNodeLinkName(),
				info.getNodePort(), 
				INFORM_NODE+" , "+infoString+" , "+nodeInfo.getNodeInfoWithHash());
	}
	
	public void stablize() throws Exception{
		if(successor==null){
			return;
		}
		String response = getResponse(this.successor.getNodeLinkName(), 
				this.successor.getNodePort(), 
				ASK+" , "+ASK_WHOS_YOUR_PREDECESSOR);
		String[] responseArray=response.split(" , ");
		if(!responseArray[2].equals(this.nodeInfo.getNodeHash())){
			if(!responseArray[0].equals("None"))
				this.successor=new NodeInfo(responseArray[0], new Integer(responseArray[1]));
			response = getResponse(this.successor.getNodeLinkName(), 
				this.successor.getNodePort(), 
				INFORM_NODE+" , "+INFORM_NOTIFY+" , "+this.nodeInfo.getNodeInfoWithHash());		
		}
	}

	void notify(String requestString) throws Exception{
		String[] requestArray=requestString.split(" , ");
		if(this.predecessor==null || !this.predecessor.getNodeHash().equals(requestArray[4])){
			if (this.successor==null){
			this.successor=new NodeInfo(requestArray[2], new Integer(requestArray[3]));				
			}
			this.predecessor=new NodeInfo(requestArray[2], new Integer(requestArray[3]));
			//send predecessor his files.
			String response=getResponse(this.predecessor.getNodeLinkName(),
					this.predecessor.getNodePort(),
					sendPredecessorFiles(SEND_NODE_FILES));
		}
	}
	
	public String sendPredecessorFiles(String predFiles) {
		//deeepcopy
		ArrayList<StoredFile> storedFilesCopy=new ArrayList<StoredFile>();
		for(StoredFile storedFile:this.storedFiles){
			storedFilesCopy.add(storedFile);
		}
		//smallest node
		if(this.successor.getNodeHash().equals(this.predecessor.getNodeHash()) &&
			this.successor.getNodeHash().compareTo(this.nodeInfo.getNodeHash())>0){
			for (StoredFile storedFile2:storedFilesCopy) {
				//if file's hash is less than predecessor's hash or they are greater than mine
				if (storedFile2.hash.compareTo(this.predecessor.getNodeHash())<0 && storedFile2.hash.compareTo(this.nodeInfo.getNodeHash())>0){
					predFiles=predFiles.concat(" , ");
					predFiles=predFiles.concat(storedFile2.getKeyValuePair());
					this.storedFiles.remove(storedFile2);
				}
			}
			return predFiles;
		}
		//if i am the smallest node.
		for (StoredFile storedFile2:storedFilesCopy) {
			//if file's hash is less than predecessor's hash or they are greater than mine
			if (storedFile2.hash.compareTo(this.predecessor.getNodeHash())<0){
				predFiles=predFiles.concat(" , ");
				predFiles=predFiles.concat(storedFile2.getKeyValuePair());
				this.storedFiles.remove(storedFile2);
			}
			else if(this.successor.getNodeHash().equals(this.predecessor.getNodeHash()) && 
					(storedFile2).hash.compareTo(this.nodeInfo.getNodeHash())>0){
				predFiles=predFiles.concat(" , ");
				predFiles=predFiles.concat(storedFile2.getKeyValuePair());
				this.storedFiles.remove(storedFile2);				
			}
		}
		return predFiles;
	}

	public void leave() throws Exception{
		this.isAlive=false;
		//transfer all  files to successor
		if (this.successor==null){
			this.nodeServerSocket.close();
			nodeServerSocket=null;
			return;
		}
		String successorRequest=YOUR_PREDECESSOR_LEAVING;
		for(StoredFile storedFile:storedFiles){
			successorRequest=successorRequest.concat(" , ");
			successorRequest=successorRequest.concat(storedFile.getKeyValuePair());
		}
		String response=getResponse(successor.getNodeLinkName(), successor.getNodePort(), successorRequest);
		//also if there were only two nodes then.
		if(this.successor.getNodeHash().equals(this.predecessor.getNodeHash())){
			response=getResponse(predecessor.getNodeLinkName(),
			predecessor.getNodePort(), 
			INFORM_NODE+" , "+INFORM_YOU_ARE_SINGLE);			
		}
		else
			response=getResponse(predecessor.getNodeLinkName(),
				predecessor.getNodePort(), 
				INFORM_NODE+" , "+INFORM_THIS_IS_YOUR_SUCCESSOR+" , "+successor.getNodeInfoWithHash());	
		this.nodeServerSocket.close();
		nodeServerSocket=null;
	}
	
	public void printNode(){
		System.out.println("Printing Node Data.");
		System.out.println("Self");
		this.nodeInfo.printNodeInfo();
		System.out.println("Successor");
		if(successor!=null)
			this.successor.printNodeInfo();
		else
			System.out.println("\tSuccessor:None");
		System.out.println("Predecessor");
		if (predecessor!=null)
			this.predecessor.printNodeInfo();
		else
			System.out.println("\tPredecessor:None");
		System.out.println("Key-Value Pairs");
		for (StoredFile storedFile:storedFiles){
			storedFile.printStoredFile();
		}
		System.out.println("\n\n");
	}
	
	public void get(String fileName) throws Exception{
		String fileNameHash=Hash.hash(fileName);
		//search yourself. Using the wrong algorithm. 
		for (StoredFile storedFile:this.storedFiles){
			if (storedFile.hash.equals(fileNameHash)){
				System.out.println("File Found");
				this.nodeInfo.printNodeInfo();
				storedFile.printStoredFile();
				return;
			}
		}
		//if I am the only Node in the System.
		if(this.successor==null){
			System.out.println("Sorry! File Not Found in DHT");
			return;
		}
		//ask other nodes.
		String ip="";int port=0;
		String response;
		String[] responseArray;
		ip=this.successor.getNodeLinkName();
		port=successor.getNodePort();
		while(!(ip.equals(this.nodeInfo.getNodeLinkName()) && port==this.nodeInfo.getNodePort())){
			//search other nodes.
			//get node information.
			System.out.println("Asking "+ip+" "+ String.valueOf(port));
			response=getResponse(ip, port, GET_NODE_INFORMATION);
			responseArray=response.split(" , ");
			//if smallest node.
			if(responseArray[2].compareTo(responseArray[5])<0 && responseArray[2].compareTo(responseArray[8])<0){
				if(fileNameHash.compareTo(responseArray[2])<0 || fileNameHash.compareTo(responseArray[8])>0){
					response=getResponse(ip, port, ASK+" , "+ASK_YOU_GOT_THIS_FILE+" , "+fileNameHash);
					if(response.equals("YES")){
						System.out.println("File Found");
						NodeInfo someNodeInfo=new NodeInfo(ip, port);
						someNodeInfo.printNodeInfo();
						System.out.println("\tKEY: "+fileNameHash);
						System.out.println("\tVALUE: "+fileName);
						return;
					}
					break;
				}
				ip=responseArray[3];port=new Integer(responseArray[4]);
				continue;
			}
			//if regular node.
			
			if(fileNameHash.compareTo(responseArray[2])<0 && fileNameHash.compareTo(responseArray[8])>0){
				response=getResponse(ip, port, ASK+" , "+ASK_YOU_GOT_THIS_FILE+" , "+fileNameHash);
				if(response.equals("YES")){
					System.out.println("File Found");
					NodeInfo someNodeInfo=new NodeInfo(ip, port);
					someNodeInfo.printNodeInfo();
					System.out.println("\tKEY: "+fileNameHash);
					System.out.println("\tVALUE: "+fileName);
					return;
				}
				break;
			}
			ip=responseArray[3];port=new Integer(responseArray[4]);

		}
		System.out.println("Sorry! File Not Found in DHT");
	}
	
	public void put(String fileName)throws Exception{
		String fileNameHash=Hash.hash(fileName);
		//search yourself. Using the wrong algorithm.
		//ask other nodes.
		String ip="";int port=0;
		String response;
		String[] responseArray;
		ip=this.nodeInfo.getNodeLinkName();
		port=nodeInfo.getNodePort();
		if(this.successor==null || this.predecessor==null){
			StoredFile storedFile=new StoredFile(fileNameHash, fileName);
			System.out.println("File Stored.");
			System.out.println("Storing Node's Info");
			nodeInfo.printNodeInfo();
			System.out.println("Stored File Information.");
			storedFile.printStoredFile();
			this.storedFiles.add(storedFile);
			return;
		}
		do{
			response=getResponse(ip, port, GET_NODE_INFORMATION);
			responseArray=response.split(" , ");
			//smallest node
			if(responseArray[2].compareTo(responseArray[5])<0 && responseArray[2].compareTo(responseArray[8])<0){
				if(fileNameHash.compareTo(responseArray[2])<0 || fileNameHash.compareTo(responseArray[8])>0){
					response=getResponse(ip, port, STORE_FILE+" , "+fileNameHash+" , "+fileName);
					System.out.println("File Stored.");
					System.out.println("Storing Node's Info");
					NodeInfo someNodeInfo=new NodeInfo(ip, port);
					someNodeInfo.printNodeInfo();
					StoredFile storedFile=new StoredFile(fileNameHash, fileName);
					System.out.println("Stored File Information.");
					storedFile.printStoredFile();
					return;
				}
				ip=responseArray[3];port=new Integer(responseArray[4]);
				continue;
			}
			//usual nodes
			if(fileNameHash.compareTo(responseArray[2])<0 && fileNameHash.compareTo(responseArray[8])>0){
				response=getResponse(ip, port, STORE_FILE+" , "+fileNameHash+" , "+fileName);
				System.out.println("File Stored.");
				System.out.println("Storing Node's Info");
				NodeInfo someNodeInfo=new NodeInfo(ip, port);
				someNodeInfo.printNodeInfo();
				StoredFile storedFile=new StoredFile(fileNameHash, fileName);
				System.out.println("Stored File Information.");
				storedFile.printStoredFile();
				return;				
			}
			ip=responseArray[3];port=new Integer(responseArray[4]);
		}while(!(ip.equals(this.nodeInfo.getNodeLinkName()) && port==this.nodeInfo.getNodePort()));		
	}
}
