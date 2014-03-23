package edu.cooper.ece465.Master;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CubbyHole {

    private int queueSize;
    private BlockingQueue<NodeData> clientQueue;
    private PriorityBlockingQueue<NodeData> producerQueue;

    public CubbyHole(int queueSize) {
        this.queueSize = queueSize;
        this.clientQueue = new ArrayBlockingQueue<>(this.queueSize);
        this.producerQueue = new PriorityBlockingQueue<>(this.queueSize, new NodeDataComparator());
    }

    public int getProducerCount(){
        return this.producerQueue.size();
    }

    //for LB stats:
    public int getQueueRatio(){
        return clientQueue.remainingCapacity();
    }

    public NodeData getClient() { // Get Client From Queue
        NodeData ret = null;
        try{
            ret = this.clientQueue.take();
        } catch (InterruptedException e){
            System.err.println("Take error: " + e);
        }

        return ret;
    }

    public void putClient(NodeData value) { // Put Client into Queue
        try{
            this.clientQueue.put(value);
        } catch (InterruptedException e){
            System.err.println("Put error: " + e);
        }
    }

    public void putProducer(NodeData value){
        this.producerQueue.put(value);
    }

    public NodeData getProducer(){
        NodeData ret = null;
        try{
            ret = this.producerQueue.take();
        } catch (InterruptedException e){
            System.err.println("Processor Take error: " + e);
        }
        return ret;
    }
}