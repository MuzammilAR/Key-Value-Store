/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class Config {
	public static int UDP_PACKET_SIZE=50000;//bytes
	public static int MAGIC=15440;
	public static int WORD_LEN=5;//including the last 
	public static int NUM_MESSAGES_BEFORE_DEATH=4;
	public static int SEND_WAIT_INTERVAL=100;//ms
	public static int RECIEVE_WAIT_TIMEOUT=1000;//ms
	public static int SERVER_PINGER_TIMEOUT=2000;//ms
	public static int SERVER_KILLER_TIMEOUT=25000;//ms
	public static int SERVER_JOB_ASSIGNER_TIMEOUT=3000;//ms
	public static char [] POSSIBLE_INPUT_CHAR={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
		's','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T',
		'U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
	public static int WORKER_JOIN_WAIT_INTERVAL=7000;//ms
	public static int CLIENT_KILLER_TIMEOUT=600000;//ms//client must wait ten minutes, before ssaying server is down
	public static int SERVER_SERILAZATION_TIMEOUT=5000;
	public static String SERVER_SERILIZABLE_FILE="server.s";
}
