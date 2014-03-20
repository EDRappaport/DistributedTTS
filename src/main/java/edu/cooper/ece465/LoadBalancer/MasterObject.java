import java.net.*;
import java.io.*;
import java.lang.Object;

public class MasterObject{

	Socket s;
	PrintWriter pw;
	BufferedReader br;
	InetAddress ia;
	int p;

	public MasterObject(Socket s, PrintWriter pw, BufferedReader br, InetAddress ia, int p){
		this.s = s;
		this.pw = pw;
		this.br = br;
		this.ia = ia;
		this.p = p;
	}

	@Override 
	public boolean equals(Object o){
		if(o == null) return false;
		if(this.getClass() != o.getClass()) return false;
		return (ia==((MasterObject) o).ia && p==((MasterObject) o).p);
	}

}