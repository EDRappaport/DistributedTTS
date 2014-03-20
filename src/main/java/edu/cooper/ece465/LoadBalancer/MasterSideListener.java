package edu.cooper.ece465.LoadBalancer;

import edu.cooper.ece465.Master.MasterData;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class MasterSideListener extends Thread {
	private PriorityBlockingQueue<MasterData> masterQueue;
    private Map<String, MasterData> masterHashMap;
	private int portNumber;

	public MasterSideListener(int portNumber, PriorityBlockingQueue<MasterData> masterQueue,
                              Map<String, MasterData> masterHashMap) {
		this.masterQueue = masterQueue;
        this.masterHashMap = masterHashMap;
		this.portNumber = portNumber;
	}

	public void run(){
		try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Accepted New Worker Connection on Master");
                InputStream is = s.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                MasterData masterData = (MasterData) ois.readObject();
                System.out.println("Got Data Object:" + masterData.toString());
                String key = masterData.getHashKey();
                System.out.println(this.masterQueue);
                if (this.masterHashMap.containsKey(key)){
                    MasterData toRemove = this.masterHashMap.get(key);
                    this.masterQueue.remove(toRemove);
                }
                this.masterQueue.add(masterData);
                this.masterHashMap.put(key, masterData);
                System.out.println(this.masterQueue);

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