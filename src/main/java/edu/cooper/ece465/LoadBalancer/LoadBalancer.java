package edu.cooper.ece465.LoadBalancer;

import edu.cooper.ece465.Master.MasterData;
import edu.cooper.ece465.Master.MasterDataComparator;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class LoadBalancer {
	
	public static void main(String[] args) {
    	if (args.length != 2){
            System.err.println("Usage java LoadBalancer <Client Port Number> <Master Port Number>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int masterPort = Integer.parseInt(args[1]);

        Comparator<MasterData> comparator = new MasterDataComparator();
        PriorityBlockingQueue<MasterData> masterQueue = new PriorityBlockingQueue<>(10, comparator);
        Map<String, MasterData> masterHash = new HashMap<>();

    	new MasterSideListener(masterPort, masterQueue, masterHash).start();

        try {
            ServerSocket serverSocket = new ServerSocket(clientPort);
            while (true) {
                System.out.println("Listening for Clients");
                Socket socket = serverSocket.accept();

                MasterData masterFound = null;
                while (masterQueue.size() > 0){
                    masterFound = masterQueue.poll();
                    Date now = new Date();
                    if (masterFound.getExpireTime().compareTo(now) >= 0 ){ // Check if the current master data has expired
                        break;
                    }
                }

                if (masterFound != null){
                    OutputStream os = socket.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    System.out.println("Master Found: " + masterFound);
                    oos.writeObject(masterFound);
                    masterFound.setScore(Double.MIN_VALUE);
                    masterQueue.add(masterFound);
                    oos.close();
                    os.close();
                }
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
	}
}