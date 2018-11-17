/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */

class StoredFile {
	public String hash;
	public String value;

	public StoredFile(String hash, String value) {
		this.hash = hash;
		this.value = value;
	}
	public StoredFile(String value) {
		this.value = value;
		this.hash=Hash.hash(value);
	}
	
	public String getKeyValuePair(){
		return this.hash+" , "+this.value;
	}
	
	public void printStoredFile() {
		System.out.println("Stored File");
		System.out.println("\tKey: "+hash);
		System.out.println("\tValue: "+value);
	}
}
