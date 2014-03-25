package edu.cooper.ece465.Producer;

import edu.cooper.ece465.Master.NodeData;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Producer {

	private static int THREAD_POOL_SIZE;

	public static void main(String[] args){
        
		//check input
        if (args.length != 2){
            System.err.println("Usage java Producer <Master host name> <Master port number>");
            System.exit(1);
        }
        String masterHostName = args[0];
        int masterPortNumber = Integer.parseInt(args[1]);

        THREAD_POOL_SIZE =  Runtime.getRuntime().availableProcessors();

        Socket s,infoSocket,clientSocket;
        ServerSocket sRcv;
        InputStream is;
        ObjectInputStream ois;
        OutputStream os;
        ObjectOutputStream oos;
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        NodeData data, clientData;
        TTSWorker server;

        while(true){
        	System.out.println("Waiting for Next Assignment");

            try {
                //attach to Master to introduce
                s = new Socket(masterHostName,masterPortNumber); // To ProducerListener
                int originalPort = s.getLocalPort();
                s.setReuseAddress(true);
                System.out.println("Introduced to Master");

                //tell Master your info
                double load = osBean.getSystemLoadAverage();
                data = new NodeData(s.getLocalAddress().getHostName(), originalPort,
                        1, load, -1);
                os = s.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(data);
                oos.close();
                os.close();

                //acccept info from Master
                System.out.println("Waiting for assignment");
                sRcv = new ServerSocket(originalPort);
                infoSocket = sRcv.accept();
                is = infoSocket.getInputStream();
                ois = new ObjectInputStream(is);
                clientData = (NodeData) ois.readObject();

                String hostName = clientData.getHostname();
                int clientPort = clientData.getPortNumber()+2;

                sRcv.close();
                infoSocket.close();

                System.out.println("attempting to connect to client: " + clientData);
                clientSocket = new Socket(hostName, clientPort);

                server = new TTSWorker();
                server.spawnProtocolHandler(clientSocket);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

	}

}
