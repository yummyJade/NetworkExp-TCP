package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class CycleQueue{

    private TCP_PACKET[] arr;
    private int max;
    private int num;
    public int send_base;
    private int rec_base;
    private int winsize;
    public int nextSeqNum;
    private UDT_Timer[] timerArr;
    private boolean[] isAcked;

    public int getSend_base() {
        return send_base;
    }

    public void setSend_base(int send_base) {
        this.send_base = send_base;
    }

    public boolean isAcked(int index) {
        return this.isAcked[index];
    }

    public void setAcked(boolean acked, int index) {
        isAcked[index] = acked;
    }

    public int getNextSeqNum() {
        return nextSeqNum;
    }

    public void setNextSeqNum(int nextSeqNum) {
        this.nextSeqNum = nextSeqNum;
    }

    public int getRec_base() {
        return rec_base;
    }

    public void setRec_base(int rec_base) {
        this.rec_base = rec_base;
    }

    public int getWinsize() {
        return winsize;
    }

    public CycleQueue(int n, int ws) {
        arr = new TCP_PACKET[n];
        timerArr = new UDT_Timer[n];
        num = 0;
        max = n;
        winsize = ws;
        nextSeqNum = 0;
        send_base = 0;
        rec_base = 0;
        isAcked = new boolean[n];
    }


    public void setTimer(UDT_Timer timer, int index) {
        timerArr[index] = timer;
    }

    public UDT_Timer getTimer(int index){
        return timerArr[index];
    }
    public boolean enqueue(TCP_PACKET data, int index){
        arr[index] = data;
        return true;
    }

    public TCP_PACKET search(int index){
        return arr[index];
    }

    public int getLength(){
        return max;
    }
    public int getNoAckLength(){
        return (nextSeqNum-send_base + max )% max;
    }

    public int getLengthBwtAB(int a, int b){return (a - b + max ) % max;}
    public boolean isCBwtAB(int a, int b, int c) {
        int lengthAC = getLengthBwtAB(c, a);
        int lengthBC = getLengthBwtAB(b, c);
//        System.out.println("lengthAC is:"+ lengthAC);
//        System.out.println("lengthBC is:"+ lengthBC);
        if(lengthAC <= getWinsize() && lengthBC <= getWinsize()){
            return true;
        }else {
            return false;
        }
    }


    public static void main(String[] args){
        CycleQueue c = new CycleQueue(10,5);
//        c.enqueue(22);

    }
}


