package edu.cooper.ece465.Master;

import java.io.Serializable;
import java.util.Date;

public class MasterData implements Serializable{

    private String hostname;
    private int portNumber;
    private double score;
    private Date expireTime;

    public MasterData(String hostname, int portNumber, float queueRatio, double systemLoad, int timeToLive){
        this.hostname = hostname;
        this.portNumber = portNumber;
        this.score = computeScore(queueRatio, systemLoad);
        this.expireTime = new Date();
        this.expireTime.setTime(this.expireTime.getTime() + timeToLive);
    }

    private double computeScore(float queueRatio, double systemLoad){
        return ((1/queueRatio) + (1/systemLoad));
    }

    public String getHostname(){
        return this.hostname;
    }

    public int getPortNumber(){
        return this.portNumber;
    }

    public double getScore(){
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Date getExpireTime(){
        return this.expireTime;
    }

    public String getHashKey(){
        return this.hostname + ":" + this.portNumber;
    }

    @Override
    public String toString(){
        return this.hostname + ":" + this.portNumber + " score: " + this.score;
    }
}
