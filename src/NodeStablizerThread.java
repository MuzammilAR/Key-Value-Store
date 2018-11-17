/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class NodeStablizerThread extends Thread{

	public Node node;

	public NodeStablizerThread(Node node) {
		this.node = node;
	}
	
	public synchronized void run(){
		while (this.node.isAlive){
			try {
				if(this.node.successor!=null)
					this.node.stablize();
				this.node.printNode();
				try {wait(20*1000);} catch (InterruptedException ex) {}
				if (!this.node.isAlive) break;
			} catch (Exception ex) {Logger.getLogger(NodeStablizerThread.class.getName()).log(Level.SEVERE, null, ex);
}
		}
	}
}
