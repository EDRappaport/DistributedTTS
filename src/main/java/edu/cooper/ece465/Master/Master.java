package edu.cooper.ece465.Master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Master {
	
    public static void main(String[] args) {
    	if (args.length != 4){
            System.err.println("Usage: java Master <Client Port Number> <Producer Port Number> <LB Hostname> <LB Port>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int producerPort = Integer.parseInt(args[1]);
        String loadBalancerHost = args[2];
        int loadBalancerPort = Integer.parseInt(args[3]);

    	CubbyHole cubbyHole = new CubbyHole(50);

        new LBChatter(cubbyHole, loadBalancerHost,loadBalancerPort, clientPort).start();
        new ProducerListener(cubbyHole, producerPort).start();
//    	new ClientHandler(cubbyHole, clientPort).start();
        new ClientProducerAssigner(cubbyHole).start();
        try (ServerSocket serverSocket = new ServerSocket(clientPort)) {
            while (true) {
                System.out.println("Listening for Clients");
                Socket s = serverSocket.accept();
                new Thread(new ClientHandler(s, cubbyHole)).start();
            }
        } catch (IOException e) {
            System.err.println("ClientHandler Error: "+e);
            System.exit(-1);
        }
    }
}
