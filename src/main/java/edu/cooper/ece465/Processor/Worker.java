package edu.cooper.ece465.Processor;

import edu.cooper.ece465.Master.NodeData;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Worker{

	private static int THREAD_POOL_SIZE;

	public static void main(String[] args){
        
		//check input
        if (args.length != 2){
            System.err.println("Usage java Worker <Master host name> <Master port number>");
            System.exit(1);
        }
        String masterHostName = args[0];
        int masterPortNumber = Integer.parseInt(args[1]);

        THREAD_POOL_SIZE =  Runtime.getRuntime().availableProcessors();

        while(true){
        	System.out.println("Waiting for Next Assignment");

            try {
                //attach to Master to introduce
                Socket s = new Socket(masterHostName,masterPortNumber); // To EqualizerListener
                int originalPort = s.getLocalPort();
                s.setReuseAddress(true);
                System.out.println("Introduced to Master");

                //tell Master your info
                OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                double load = osBean.getSystemLoadAverage();
                NodeData data = new NodeData(s.getLocalAddress().getHostName(), originalPort,
                        1, load, -1);
                OutputStream os = s.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(data);
                oos.close();
                os.close();

                //acccept info from Master
                ServerSocket sRcv = new ServerSocket(originalPort);
                Socket infoSocket = sRcv.accept();
                InputStream is = infoSocket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                NodeData clientData = (NodeData) ois.readObject();

                String hostName = clientData.getHostname();
                int clientPort = clientData.getPortNumber();

                sRcv.close();
                infoSocket.close();

                Socket clientSocket = new Socket(hostName, clientPort);

                
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
