package edu.cooper.ece465.Master;

import java.net.*;
import java.io.*;

public class ClientListener extends Thread {
	private int portNumber;
	private CubbyHole cubbyhole;

    public ClientListener(CubbyHole c, int port) {
        this.cubbyhole = c;
        this.portNumber = port;
        cubbyhole.addProducer();
    }

    private void putData(Data data) {
        cubbyhole.put(data);
    }

    private int checkAvailability(int numRequested){ 
        int numGranted;
        int numProcessors = cubbyhole.getProcessorsCount();
        if (numRequested > numProcessors) numGranted = numProcessors;
        else numGranted = numRequested;

        return numGranted;
    }
    
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();
                BufferedReader inFromC = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
                PrintWriter outToC = new PrintWriter(s.getOutputStream(), true);

                int minRequest = Integer.parseInt(inFromC.readLine());
                int numRequested = Integer.parseInt(inFromC.readLine());

                System.out.println("minRequest: "+minRequest+"; numRequested: "+numRequested);
                int numGranted = checkAvailability(numRequested);
                if (numGranted < minRequest) numGranted = minRequest;
                outToC.println(numGranted);
                System.out.println("numGranted: "+numGranted);

                int p = s.getPort();
                System.out.println("New Client at Port "+p);
                InetAddress ia = s.getInetAddress();
                s.close();
                
                for (int i = 0; i<numGranted; i++){
                    putData(new Data(ia,p,0,-1));
                }
            }
        } catch (IOException e) {
            System.err.println("ClientListener Error: "+e);
            System.exit(-1);
        }
        
        cubbyhole.subProducer();
    }
}