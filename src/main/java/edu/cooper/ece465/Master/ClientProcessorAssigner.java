package edu.cooper.ece465.Master;

import java.net.*;
import java.io.*;

public class ClientProcessorAssigner extends Thread {
	private CubbyHole cubbyhole;

	public ClientProcessorAssigner(CubbyHole c){
		this.cubbyhole = c;
	}

    private void assignProducer(NodeData client) throws IOException {
        NodeData curProcessor = this.cubbyhole.getProducer();
        Socket s = new Socket(curProcessor.getHostname(), curProcessor.getPortNumber());
        System.out.println("Sending New Assignment");
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(client);
        oos.close();
        s.close();
    }

	public void run(){
        NodeData client = this.cubbyhole.getClient();
        while(true){
            try {
                assignProducer(client);
                client = this.cubbyhole.getClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Sent New Assignment");
        }
	}
}