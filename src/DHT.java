/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 * Muzammil Abdul Rehman
 * Rana Zohaib ur Rehman
 */
public class DHT {

	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		System.out.println("\t\t\tWelcome to the DHT.");
		System.out.println("*Press 1 to Create");
		System.out.println("*Press 2 to Join");
		int i=scanner.nextInt();
		int port=0;
		String hostip="";
		int hostport=0;
		Node currentNode=new Node();
		System.out.println("Please Enter Your Port Number.");
		port=scanner.nextInt();
		scanner=new Scanner(System.in);
		try {
			System.out.println(i);
			if(i==1){
					currentNode.create(port);
			}else if(i==2){
					System.out.println("Please Enter IP of the Node to join:");
					hostip=scanner.nextLine();
					System.out.println("Please Enter the Port of the Node to join:");
					hostport=scanner.nextInt();
					currentNode.join(port, hostip, hostport);
			}else
					System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		while(currentNode.isAlive){
			try {
				scanner=new Scanner(System.in);
				System.out.println("*Please Choose an option:");
				System.out.println("*Press 1 to put file in DHT");
				System.out.println("*Press 2 to get file from DHT");
				System.out.println("*Press 3 to leave the DHT");
				i=scanner.nextInt();
				String fName;
				if(i==1){
					scanner=new Scanner(System.in);
					System.out.println("Please Enter the file name to add:");
					fName=scanner.nextLine();
					System.out.println(fName);
					currentNode.put(fName);
				}else if (i==2){
					scanner=new Scanner(System.in);
					System.out.println("Please Enter the file name to Search:");
					fName=scanner.nextLine();
					currentNode.get(fName);
				}else if (i==3){
						currentNode.leave();
				}else if (i==4){
						currentNode.printNode();
				}else 
					continue;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
