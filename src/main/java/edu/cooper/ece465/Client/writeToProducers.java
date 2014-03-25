package edu.cooper.ece465.Client;

import java.io.IOException;
import java.net.*;
import java.io.*;

import com.sun.speech.freetts.util.Utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class writeToProducers extends Thread{
    private ServerSocket sSend;
	private String[] fileNames;
	private int[] fileSplits;
	private int returnPort;
	private String in_dir;

	private static final int AUDIO_BUFFER_SIZE = 256;
    private int sampleRate = Utilities.getInteger("sampleRate", 16000).intValue();
    private int sampleSize = 16;            // in bits
    private byte[] socketBuffer = new byte[AUDIO_BUFFER_SIZE];	
    private BufferedReader reader;
    private DataInputStream dataReader;     
    private PrintWriter writer;

	public writeToProducers(ServerSocket sSend, String[] fileNames, int[] fileSplits, int returnPort, String in_dir){
		this.sSend = sSend;
		this.fileNames = fileNames;
		this.fileSplits = fileSplits;
		this.returnPort = returnPort;
		this.in_dir = in_dir;
	}

	//copied from Sun Microsystems, Inc
    private String readLine() throws IOException {
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

    //copied from Sun Microsystems, Inc
    private void sendLine(String line) {
		line = line.trim();
		if (line.length() > 0) {
		    writer.print(line);
		    writer.print('\n');
		    writer.flush();
		}
    }

	private static ArrayList<String> splitFile(BufferedReader fileReader, int numPieces) throws IOException {
        ArrayList<String> allLines = new ArrayList<String>();
		String line = fileReader.readLine();
		for (int i = 0; line != null; i++){
			allLines.add(line);
			line = fileReader.readLine();
		}

		int numLines = allLines.size();
		ArrayList<String> textPieces = new ArrayList<String>(numLines);
		int linesPerWorker = numLines/numPieces;
		int i, j;
		//ex. 107 totalLines: 10 pieces each of 10 lines,
		//last piece of remaining 7 lines
		for (i = 0, j = 0; i<numPieces-1; i++){
			textPieces.add("");
			for (int k = 0; k<linesPerWorker; k++){
				textPieces.add(textPieces.get(i)+allLines.get(j++));
			}
		}
        textPieces.add("");
		for (int k = j; k < numLines; k++){
			textPieces.set(i, textPieces.get(i)+allLines.get(j++));
		}

		return textPieces;
	}

	public void run(){

		try{
			BufferedReader fileReader = null;		

			//loop through images in directory
	        for(int i = 0; i < fileNames.length; i++) {
	        	String fileName = this.in_dir + fileNames[i];
	        	int numPieces = fileSplits[i] + 1;

	            fileReader = new BufferedReader(new FileReader(fileName));
	            ArrayList<String> textPieces = splitFile(fileReader, numPieces);

	            for (int j = 0; j<textPieces.size(); j++){
		            Socket giveTextSocket = this.sSend.accept();
		            System.out.println("Connected to Processing Server");
	            	writer = new PrintWriter(giveTextSocket.getOutputStream(), true);
					// send TTS request to server
					sendLine("TTS\n" +
						 String.valueOf(this.sampleRate) + "\n" +
						 fileName + "\n" + j + "\n" + giveTextSocket.getLocalAddress().getHostName() + "\n" + returnPort + "\n" +
						 textPieces.get(j) + "\n" + "DONE");

					giveTextSocket.close();
	            }
	        }
	        this.sSend.close();
	    } catch (IOException e){
	    	System.err.println("writeToProducers Error: "+e);
	    	System.exit(-1);
	    }
	}

}