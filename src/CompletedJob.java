
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
class CompletedJob implements Serializable{
	private static final long serialVersionUID = 71234342L;
	private String hash;
	private char [] value;

	public CompletedJob(String hash, char [] value) {
		this.hash = hash;
		this.value = value;
	}
	
	public char [] findMatch(String matchString){
		if(hash.equals(matchString)){
			return value;
		}
		return null;
	}	
}
