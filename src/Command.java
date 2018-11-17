/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public enum Command {
	REQUEST_TO_JOIN, 
	JOB, 
	ACK_JOB, 
	PING,
	DONE_NOT_FOUND, 
	DONE_FOUND, 
	NOT_DONE, 
	CANCEL_JOB,
	HASH,
	JOINED
	;

	private Command() {
	}
}
