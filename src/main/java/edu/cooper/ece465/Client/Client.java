package edu.cooper.ece465.Client;

import edu.cooper.ece465.Master.NodeData;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * Implements a Java Client for the Client/Server demo. For details about
 * the protocol between client and server, consult the file
 * <code>Protocol.txt</code>.
 */
public class Client {

    public static String[] parseDirectory(String dir_string){
        File dir = new File(dir_string);
        if(!dir.isDirectory()){
            System.out.println("Invalid directory entered");
            System.exit(-1);
        }

        String[] dirFiles = dir.list(new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".txt");
            }
        });

        return dirFiles;
    }


	public static void main(String[] args) throws FileNotFoundException {

		//check input
        if (args.length != 4){
            System.err.println("Usage java Client <LB host name> <LB port number> <original directory> <output directory>");
            System.exit(1);
        }
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String in_dir = args[2];
        String out_dir = args[3];

        //get all input (.txt) files from in_dir
        String[] fileNames = parseDirectory(in_dir);
        File out_dir_f = new File(out_dir);
        if (!out_dir_f.exists()){
            if(!out_dir_f.mkdir() && !out_dir_f.mkdirs()){
                System.out.println("Could Not Create Directory");
                System.exit(-1);
            }
        } else if(!out_dir_f.isDirectory()){
            System.out.println("Please input a directory name for output");
            System.exit(-1);
        }

        //get fileSizes and decide to split
        int[] fileSplits = new int[fileNames.length];
        int minRequest, numRequested = 0;
        for(int i=0; i<fileNames.length; i++){
        	BufferedReader reader = new BufferedReader(new FileReader(fileNames[i]));
        	int numLines = 0;
            try {
                while (reader.readLine() != null) numLines++;
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            fileSplits[i] = numLines/10;
        	numRequested += fileSplits[i] + 1;
        }
        minRequest = fileNames.length; //need at least minRequest Workers

		//connect to Load Balancer
        System.out.println("Connecting To Load Balancer");
        Socket sLB = null;
        try {
            sLB = new Socket(hostName, portNumber);
            InputStream is = sLB.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            NodeData nodeData = (NodeData) ois.readObject();
            System.out.println("Got Data Object:" + nodeData.toString());
            is.close();
            sLB.close();

            //initiate client socket to Master
            System.out.println("Connecting To Master Server");
            Socket s = new Socket(nodeData.getHostname(), nodeData.getPortNumber());

            //request numRequested from Master
            BufferedReader inFromM = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
            PrintWriter outToMaster = new PrintWriter(s.getOutputStream(), true);
            outToMaster.println(minRequest);
            outToMaster.println(numRequested);
            int numGranted = Integer.parseInt(inFromM.readLine());

            //adjust fileSplits
            for (int i = 0, j = 0; i < numRequested - numGranted; i++, j++){
                if (fileSplits[j] > 0) fileSplits[j]--;
                else i--;
                if (j == fileSplits.length - 1) j = 0;
            }

            int originalPort = s.getLocalPort();
            int returnPort = originalPort+1;
            s.setReuseAddress(true);

            new writeToProcessors(originalPort, fileNames, fileSplits, returnPort, in_dir).start();


            //wait for returned pieces
            ServerSocket sRcv = new ServerSocket(returnPort);
            for (int i = 0; i < numGranted; i++){
                Socket rcvSocket = sRcv.accept();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
	}
}
