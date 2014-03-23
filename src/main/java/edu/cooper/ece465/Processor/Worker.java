package edu.cooper.ece465.Processor;

import edu.cooper.ece465.Master.MasterData;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import com.sun.speech.freetts.util.Utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Worker{

	private static int THREAD_POOL_SIZE;

	public static void main(String[] args){
        
		//check input
        if (args.length != 2){
            System.err.println("Usage java Worker <Master host name> <Master port number>");
            System.exit(1);
        }
        masterHostName = args[0];
        masterPortNumber = Integer.parseInt(args[1]);

        THREAD_POOL_SIZE =  Runtime.getRuntime().availableProcessors();

        while(true){
        	System.out.println("Waiting for Next Assignment");

            //attach to Master to introduce
            Socket s = new Socket(masterHostName,masterPortNumber); // To EqualizerListener 
            int originalPort = s.getLocalPort();
            s.setReuseAddress(true);
            System.out.println("Introduced to Master");

            //tell Master your info
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double load = osBean.getSystemLoadAverage();
            MasterData data = new MasterData(s.getLocalAddress().getHostName(), originalPort,
                    1, load, -1);
            OutputStream os = s.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(data);
            oos.close();
            os.close();

            //acccept info from Master
            ServerSocket sRcv = new ServerSocket(originalPort);
            Socket infoSocket = sRcv.accp();
            InputStream is = infoSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            MasterData clientData = (MasterData) ois.readObject();

            String hostName = clientData.getHostname();
            int clientPort = clientData.getPortNumber();

            sRcv.close();
            infoSocket.close();

            



        }

	}

}
