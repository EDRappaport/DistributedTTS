package edu.cooper.ece465.Client;

import com.sun.speech.freetts.Tokenizer;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import com.sun.speech.freetts.audio.RawFileAudioPlayer;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import edu.cooper.ece465.Master.NodeData;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Implements a Java Client for the Client/Server demo. For details about
 * the protocol between client and server, consult the file
 * <code>Protocol.txt</code>.
 */
public class Client {

    static private DataInputStream dataReader;
    private static final int AUDIO_BUFFER_SIZE = 256;
    private byte[] socketBuffer = new byte[AUDIO_BUFFER_SIZE];

    private static String[] parseDirectory(String dir_string){
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

    private static int countNumLinesEfficient(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }


    //copied from Sun Microsystems, Inc
    static private String readLine() throws IOException {
        int i;
        char c;
        StringBuffer buffer = new StringBuffer();

        while ((c = (char) dataReader.readByte()) != '\n') {
            buffer.append(c);
        }

        int lastCharIndex = buffer.length() - 1;
        
        // remove trailing ^M for Windows-based machines
        byte lastByte = (byte) buffer.charAt(lastCharIndex);
        if (lastByte == 13) {
            return buffer.substring(0, lastCharIndex);
        } else {
            return buffer.toString();
        }
    }


    private static ArrayList<Byte> receiveAndStore(int numberSamples) {

    int bytesToRead;
    int bytesRemaining;

    ArrayList<Byte> allBytes = new ArrayList<>();

    bytesRemaining = numberSamples;

    while (bytesRemaining > 0) {

        try{
            allBytes.add(dataReader.readByte());
            bytesRemaining--;
        } catch (IOException ioe) {
        ioe.printStackTrace();
        }
    }
    return allBytes;
    }

    public static void main(String[] args) throws FileNotFoundException {

        long startTime = System.currentTimeMillis();
        System.out.println("startTime: "+startTime);

        //check input
        if (args.length != 4){
            System.err.println("Usage java Client <LB host name> <LB port number> <original directory> <output directory>");
            System.exit(1);
        }
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String inputDirectory = args[2];
        String outputDirectory = args[3];

        //getClient all input (.txt) files from inputDirectory
        String[] fileNames = parseDirectory(inputDirectory);
        File outDirectoryFile = new File(outputDirectory);
        if (!outDirectoryFile.exists()){
            if(!outDirectoryFile.mkdir() && !outDirectoryFile.mkdirs()){
                System.out.println("Could Not Create Directory");
                System.exit(-1);
            }
        } else if(!outDirectoryFile.isDirectory()){
            System.out.println("Please input a directory name for output");
            System.exit(-1);
        }

        //getClient fileSizes and decide to split
        int[] fileSplits = new int[fileNames.length];
        int minRequest, numRequested = 0;
        for(int i=0; i<fileNames.length; i++){
            int numLines = 0;
            try {
                numLines = countNumLinesEfficient(inputDirectory + fileNames[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            fileSplits[i] = numLines/10;
            numRequested += fileSplits[i] + 1;
        }
        minRequest = fileNames.length; //need at least minRequest Workers

        //connect to Load Balancer
        System.out.println("Connecting To Load Balancer");
        try {
            Socket sLB = new Socket(hostName, portNumber);
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
            DataInputStream dataFromMaster = new DataInputStream(s.getInputStream());
            DataOutputStream dataToMaster = new DataOutputStream(s.getOutputStream());

            // TODO: Use better logic for originalPort (also implemented in Producer)
            ServerSocket clientSocket = new ServerSocket(s.getLocalPort()+2);

            dataToMaster.writeInt(minRequest);
            dataToMaster.writeInt(numRequested);
            int numGranted = dataFromMaster.readInt();

            //adjust fileSplits
		System.out.println(fileSplits.length);
        System.out.println(fileSplits[0]);
            for (int i = 0, j = 0; i < numRequested - numGranted; i++, j++){
                System.out.println(fileSplits[0]);
                if (fileSplits[j] > 0) fileSplits[j]--;
                else i--;
                if (j == fileSplits.length - 1) j = -1;
            }
            System.out.println(fileSplits[0]);

            int originalPort = s.getLocalPort();
            int returnPort = originalPort+1; // TODO: Use better logic for returnPort
            s.setReuseAddress(true);

            //wait for returned pieces
            ServerSocket sRcv = new ServerSocket(returnPort);

            new writeToProducers(clientSocket, fileNames, fileSplits, returnPort, inputDirectory).start();

            Map<String, ArrayList<Byte>> allData = new HashMap<>();
            for (int i = 0; i < numGranted; i++){
                Socket rcvSocket = sRcv.accept();
                dataReader = new DataInputStream(rcvSocket.getInputStream());

                String fileName = readLine();
                String partNumber = readLine();

                System.out.println("FN: "+fileName+" ; PN: "+partNumber);

                String numberSamplesStr = readLine();
                int numberSamples = Integer.parseInt(numberSamplesStr);
                
                if (numberSamples == -2) { // error
                    System.err.println("Client.sendTTSRequest(): error!");
                    System.exit(-1);
                }           
                if (numberSamples > 0) {
                    System.out.println
                    ("Receiving : " + numberSamples + " samples");
                    ArrayList<Byte> receivedBytes = receiveAndStore(numberSamples);
                    allData.put(fileName+":"+partNumber, receivedBytes);
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("endTime: "+endTime);
            long runTime = endTime - startTime;
            System.out.println("run time: "+runTime);

            for (int i = 0; i<fileNames.length; i++){
                ArrayList<Byte> curBytes = new ArrayList<>();
                for(int j = 0; j<fileSplits[i] + 1; j++){
                    curBytes.addAll(allData.get(inputDirectory + fileNames[i]+":"+j));
                }
//                File dstFile = new File(outputDirectory+"/"+fileNames[i]+".wav");
//                FileOutputStream out = new FileOutputStream(dstFile);
                byte[] b = new byte[curBytes.size()];
                for (int k=0; k<b.length; k++) {
                    b[k] = curBytes.get(k);
                }
//              out.write(b);
//                out.close();

                SingleFileAudioPlayer sfap = new SingleFileAudioPlayer(outputDirectory+"/"+fileNames[i], AudioFileFormat.Type.WAVE);
                sfap.setAudioFormat
                    (new AudioFormat(16000, 16, 1, true, true));
                sfap.begin(b.length);
                sfap.write(b);
                sfap.end();
                sfap.close();

                AudioPlayer audioPlayer = new JavaStreamingAudioPlayer();
                audioPlayer.setAudioFormat
                        (new AudioFormat(16000, 16, 1, true, true));
                audioPlayer.begin(b.length);
                audioPlayer.write(b);
                audioPlayer.end();
                audioPlayer.drain();
                audioPlayer.close();


            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
