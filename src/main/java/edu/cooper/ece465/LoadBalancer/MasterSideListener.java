package edu.cooper.ece465.LoadBalancer;

import edu.cooper.ece465.Master.NodeData;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class MasterSideListener extends Thread {
	private PriorityBlockingQueue<NodeData> masterQueue;
    private Map<String, NodeData> masterHashMap;
	private int portNumber;

	public MasterSideListener(int portNumber, PriorityBlockingQueue<NodeData> masterQueue,
                              Map<String, NodeData> masterHashMap) {
		this.masterQueue = masterQueue;
        this.masterHashMap = masterHashMap;
		this.portNumber = portNumber;
	}

	public void run(){
		try {
            ServerSocket serverSocket = new ServerSocket(this.portNumber);
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Accepted New Worker Connection on Master");
                InputStream is = s.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                NodeData nodeData = (NodeData) ois.readObject();
                System.out.println("Got Data Object:" + nodeData.toString());
                String key = nodeData.getHashKey();
                System.out.println(this.masterQueue);
                if (this.masterHashMap.containsKey(key)){
                    NodeData toRemove = this.masterHashMap.get(key);
                    this.masterQueue.remove(toRemove);
                }
                this.masterQueue.add(nodeData);
                this.masterHashMap.put(key, nodeData);
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