
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class Worker extends Client implements Serializable{
	private static final long serialVersionUID = 278453956324L;
	public boolean jobRecievedByWorker;
	public Worker(NodeInfo info) {
		super(info);
		jobRecievedByWorker=false;
	}

	public void printEverything(){
		System.out.print("ID: ");
		System.out.println(id);
		info.printNodeInfo();
		if(job!=null)
			job.printEveryting();
		System.out.print("\tJob Recieved: ");
		System.out.println(jobRecievedByWorker);
		System.out.println("");
	}
}
