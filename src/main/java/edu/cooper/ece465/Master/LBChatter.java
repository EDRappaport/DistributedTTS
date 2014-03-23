package edu.cooper.ece465.Master;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;

public class LBChatter extends Thread {
    private CubbyHole c;
    private String hostName;
    private int portNumber;
    private int clientPort;
    private int timeToLiveMS = 10000;
    private int latency = 1000;

    public LBChatter(CubbyHole cubbyhole, String host, int port, int clientPort) {
        c = cubbyhole;
        hostName = host;
        portNumber = port;
        this.clientPort = clientPort;
    }

    public void run() {
        try{
            while (true) {
                OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                double load = osBean.getSystemLoadAverage();
                Socket socketLB = new Socket(hostName, portNumber);
                NodeData data = new NodeData(socketLB.getLocalAddress().getHostName(), this.clientPort,
                        this.c.getQueueRatio(), load, this.timeToLiveMS);
                OutputStream os = socketLB.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(data);
                oos.close();
                os.close();
                System.out.println("Master sent to loadbalancer: " + data.toString());
                Thread.sleep(this.timeToLiveMS - this.latency);
            }
        } catch (IOException e){
            System.err.println("LB Chatter error: "+e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}