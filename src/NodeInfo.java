/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.InetAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 **/

public class NodeInfo {
	private int port;
	private InetAddress ip;
	private String nodeHash;

	public NodeInfo(InetAddress ip, int port, String nodeHash) {
		this.port = port;
		this.ip = ip;
		this.nodeHash = nodeHash;
	}

	public NodeInfo(InetAddress ip, int port) {
		this.port = port;
		this.ip = ip;
		this.makeHash();
	}

	public NodeInfo(String ip, int port) throws UnknownHostException {
		this.ip = (InetAddress)InetAddress.getByName(ip);
		this.port = port;
		this.makeHash();//this is not necessary
		printNodeInfo();
	}

	private NodeInfo(String ipPortString) throws UnknownHostException{
		this.setNodeInfo(ipPortString);
	}

	private void makeHash(){
		this.nodeHash=Hash.hash(this.ip.getHostAddress()+","+String.valueOf(this.port));
	}
	
	public NodeInfo() {
		this.ip =null;
		this.port = 0;
		this.nodeHash="";
	}
	
	public boolean isNull(){
		return this.ip==null;
	}
	
	public void setNull(){
		this.ip=null;this.port=0;this.nodeHash="";
	}
	
	public void setNodeInfo(String ipPortString) throws UnknownHostException{
		String[] ipPortArray=ipPortString.split(",");
		this.ip=(InetAddress)InetAddress.getByName(ipPortArray[0]);
		Integer integer=new Integer(ipPortArray[1]);
		this.port=integer;
		this.makeHash();//this is not necessary
		System.out.println("\tNode Info Updated.");
		printNodeInfo();
	}
	
	public String getNodeInfo(){
		return this.ip.getHostAddress()+","+ String.valueOf(port);
	}
	
	public String getNodeInfoWithHash(){
		return this.ip.getHostAddress()+" , "+ String.valueOf(port)+" , "+nodeHash;
	}

	public String getNodeLinkName(){
		return this.ip.getHostAddress();
	}
	
	public int getNodePort(){
		return this.port;
	}
	
	public String getNodeHash(){
		return nodeHash;
	}

	public void printNodeInfo() {
		System.out.println("\tIP: "+ip.getHostAddress());
		System.out.println("\tPort: "+String.valueOf(port));
		System.out.println("\tHash: "+this.nodeHash);
	}
}
