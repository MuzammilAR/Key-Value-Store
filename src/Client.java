
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class Client implements Serializable{
	private static final long serialVersionUID = 1254236243L;
	public NodeInfo info;
	public Message job;
	public int id;
	private int life;

	public Client(NodeInfo info) {
		id=0;
		this.info = info;job=null;
		life=10;
	}

	public Client() {
		id=0;
		this.info = null;job=null;
		life=10;
	}

	public Client(NodeInfo info, Message job, int id) {
		this.info = info;
		this.job = job;
		this.id = id;
		life=10;
	}

	public int getLife() {
		return life;
	}
	
	public void cleanLife(){
		life=0;
	}
	
	public void callExtraLife() {
		life+=10;
	}

	void printEverything() {
		System.out.print("ID: ");
		System.out.println(id);
		info.printNodeInfo();
		job.printEveryting();
		System.out.println("");
	}
	
	
}
