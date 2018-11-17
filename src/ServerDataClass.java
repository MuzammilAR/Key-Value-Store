
import java.io.Serializable;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class ServerDataClass implements Serializable{
	private static final long serialVersionUID = 1212387L;
	public ArrayList<Client> clients;
	public ArrayList<Worker> workers;
	public ArrayList<Message> jobsLeft;
	public ArrayList<CompletedJob> completedJobs;
	public ArrayList<char[]> possibleJobs;
	public boolean alive;
	public int clientID;
	public int port;

	public ServerDataClass(ServerClass server) {
		this.clients=server.clients;
		this.workers=server.workers;
		this.jobsLeft=server.jobsLeft;
		this.completedJobs=server.completedJobs;
		this.possibleJobs=server.possibleJobs;
		this.alive=server.isAlive();
		this.clientID=server.getClientID();
		this.port=server.datagramSocket.getLocalPort();
	}
	
	

}
