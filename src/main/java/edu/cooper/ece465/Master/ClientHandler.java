package edu.cooper.ece465.Master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
	private CubbyHole cubbyhole;

    public ClientHandler(Socket socket, CubbyHole cubbyHole) {
        this.socket = socket;
        this.cubbyhole = cubbyHole;
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
        try {
            System.out.println("Accepted new client");
            DataInputStream inFromClient = new DataInputStream(this.socket.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(this.socket.getOutputStream());

            int minRequest = inFromClient.readInt();
            int numRequested = inFromClient.readInt();

            System.out.println("minRequest: "+minRequest+"; numRequested: "+numRequested);
            int numGranted = checkAvailability(numRequested);
            if (numGranted < minRequest) numGranted = minRequest;
            outToClient.writeInt(numGranted);
            System.out.println("numGranted: "+numGranted);

            int port = this.socket.getPort();
            System.out.println("New Client at Port " + port);
            String clientHostname = this.socket.getInetAddress().getHostName();
            this.socket.close();

            for (int i = 0; i<numGranted; i++){
                this.cubbyhole.putClient(new NodeData(clientHostname, port, -1, -1, -1));
            }
        } catch (IOException e) {
            System.err.println("ClientHandler Error: "+e);
            System.exit(-1);
        }
    }
}