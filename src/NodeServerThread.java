/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 * 
 */

public class NodeServerThread extends Thread{
	public ServerSocket serverSocket;
	public Node node;
	Socket clientSocket;
	NodeClientThread nodeClientThread;
	
	public NodeServerThread(ServerSocket serverSocket,Node node){
		this.serverSocket=serverSocket;
		this.node=node;
		this.clientSocket=null;
	}
	
	@Override
	public synchronized void run(){
		while((!serverSocket.isClosed())&& this.node.isAlive){
			try {
				clientSocket=serverSocket.accept();
				nodeClientThread =new NodeClientThread(clientSocket, node);
				nodeClientThread.node=this.node;
				nodeClientThread.start();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.out.println("Sorry! An Incoming Connection was Not Made.");
			}
		}
		//after closing we have to do something, like gracefully, leave,etc
		try {serverSocket.close();} catch (IOException ex) {}
	}
}

