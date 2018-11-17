
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Muzammil Abdul Rehman <muzammil.abdul.rehman@gmail.com>
 */
public class ServerSerializerThread implements Runnable{
	public ServerClass server;

	public ServerSerializerThread(ServerClass server) {
		this.server = server;
	}

	synchronized public void run() {
		ServerDataClass serverDataClass=null;
		while(server.isAlive()){
			try {
				wait(Config.SERVER_SERILAZATION_TIMEOUT);
			} catch (Exception e) {
			}
			serverDataClass=new ServerDataClass(server);
			try (
					FileOutputStream file = new FileOutputStream(Config.SERVER_SERILIZABLE_FILE);
					OutputStream buffer = new BufferedOutputStream(file);
					ObjectOutputStream output = new ObjectOutputStream(buffer);
						){
						 output.writeObject(serverDataClass);
			}
			catch(Exception ex){ex.printStackTrace();}
		}
	}

}
