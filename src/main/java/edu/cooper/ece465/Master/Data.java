package edu.cooper.ece465.Master;

import java.net.*;
import java.io.*;

public class Data implements Comparable<Data>{
	public InetAddress ia;
	public int portNumber;
	public int requestType; // Relevant for clientQueue objects
	public float priority; // Relevant for processorQueue objects
	// 0: Client request to proc images
	// 1: Producer request to calc histogram
	// 2: Producer request to rescale image
	
	public Data(InetAddress ia,int p, int requestType, float priority){
		this.ia = ia;
		this.portNumber = p;
		this.requestType = requestType;
		this.priority = priority;
	}
	
	public String toString(){
		return "ia: " + ia + " and port number:" + this.portNumber;
	}

	public int compareTo(Data compareData){
		float compareQuantity = compareData.priority;

		return - ((int) ((this.priority - compareQuantity)*100));
	}
}
