package edu.cooper.ece465.Master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public ClientListener(CubbyHole cubbyHole, int port) {
        this.cubbyhole = cubbyHole;
        this.portNumber = port;
    }

    private int checkAvailability(int numRequested){
        int numProducers = this.cubbyhole.getProducerCount();
        if (numRequested > numProducers) {
            return numProducers;
        } else {
            return numRequested;
        }
    }
    
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.portNumber)) {
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();
                DataInputStream inFromClient = new DataInputStream(s.getInputStream());
                DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());

                int minRequest = inFromClient.readInt();
                int numRequested = inFromClient.readInt();

                System.out.println("minRequest: "+minRequest+"; numRequested: "+numRequested);
                int numGranted = checkAvailability(numRequested);
                if (numGranted < minRequest) numGranted = minRequest;
                outToClient.writeInt(numGranted);
                System.out.println("numGranted: "+numGranted);

                int port = s.getPort();
                System.out.println("New Client at Port " + port);
                String clientHostname = s.getInetAddress().getHostName();
                s.close();
                
                for (int i = 0; i<numGranted; i++){
                    this.cubbyhole.putClient(new NodeData(clientHostname, port, -1, -1, -1));
                }
            }
        } catch (IOException e) {
            System.err.println("ClientListener Error: "+e);
            System.exit(-1);
        }
    }
}