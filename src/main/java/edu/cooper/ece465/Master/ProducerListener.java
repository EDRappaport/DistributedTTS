package edu.cooper.ece465.Master;

import java.net.*;
import java.io.*;

public class ProducerListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public ProducerListener(CubbyHole cubbyHole, int portNumber) {
        this.cubbyhole = cubbyHole;
        this.portNumber = portNumber;
    }

    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(this.portNumber);
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Have New Waiting Processing Server");
                InputStream is = s.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                NodeData producerData = (NodeData) ois.readObject();
                System.out.println("Got Data Object:" + producerData.toString());
                this.cubbyhole.putProducer(producerData);

                is.close();
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}