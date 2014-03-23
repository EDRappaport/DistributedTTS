package edu.cooper.ece465.Client;

import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import com.sun.speech.freetts.util.Utilities;

import edu.cooper.ece465.Master.MasterData;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;

import javax.sound.sampled.AudioFormat;


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


	public static void main(String[] args){

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
        int[] fileSplits;
        int minRequest, numRequested;
        for(int i=0; i<fileNames.length; i++){
        	BufferedReader reader = new BufferedReader(new FileReader(fileNames[i]));
        	int numLines = 0;
        	while (reader.readLine() != null) numLines++;
        	reader.close();

        	fileSplits[i] = numLines/10;
        	numRequested += fileSplits[i] + 1;
        }
        minRequest = fileNames.length; //need at least minRequest Workers

		//connect to Load Balancer
        System.out.println("Connecting To Load Balancer");
        Socket sLB = new Socket(hostName, portNumber);
        InputStream is = s.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        MasterData masterData = (MasterData) ois.readObject();
        System.out.println("Got Data Object:" + masterData.toString());
        is.close();
        sLB.close();

        //initiate client socket to Master
        System.out.println("Connecting To Master Server");
        Socket s = new Socket(masterData.getHostName(), masterData.getPortNumber());

        //request numRequested from Master
        BufferedReader inFromM = new BufferedReader(
            new InputStreamReader(s.getInputStream()));
        PrintWriter outToMaster = new PrintWriter(s.getOutputStream(), true);
        outToM.println(minRequest);
        outToM.println(numRequested);
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
	}
}
