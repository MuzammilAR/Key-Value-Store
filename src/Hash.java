

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 * Disclaimer:
 * This hash function has been requested from Hasnain Ali Pirzada,
 * AFTER consulting Course TA(Zaman) that "Hash Function from anyone is allowed". 
 * However, this code has been modified, 
 */

import java.security.MessageDigest;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
public class Hash {
	private static StringBuffer hashBuffer;
	public static String hash(String inputString){
		try {
			MessageDigest md = null;
			//create byte array
			md = MessageDigest.getInstance("SHA-1");
			md.reset();
			md.update(inputString.getBytes());
			byte byteData[] = md.digest();
			//convert the byte array to HEX string.
			hashBuffer = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				hashBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	   return hashBuffer.toString();
	}
}