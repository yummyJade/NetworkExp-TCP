package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_PACKET;

public class CycleQueue{
    private int front;
    private int rear;
    private TCP_PACKET[] arr;
    private int max;
    private int num;
    public int send_base;
    private int winsize;
    public int nextSeqNum;

    public int getSend_base() {
        return send_base;
    }

    public void setSend_base(int send_base) {
        this.send_base = send_base;
    }

    public int getNextSeqNum() {
        return nextSeqNum;
    }

    public void setNextSeqNum(int nextSeqNum) {
        this.nextSeqNum = nextSeqNum;
    }

    public int getWinsize() {
        return winsize;
    }

    public CycleQueue(int n, int ws) {
        arr = new TCP_PACKET[n];
        front = 0;
        rear = 0;
        num = 0;
        max = n;
        winsize = ws;
        nextSeqNum = 0;
        send_base = 0;
    }

//    public boolean enqueue(Object data)
//    {
//        if (num > 0 && front == rear) {
//           return false;
//        }else {
//            arr[rear] = data;
//            rear=(rear+1)% max;
//            num++;
//            return true;
//        }
//    }
    public boolean enqueue(TCP_PACKET data, int index){
//        if(nextSeqNum < send_base + winsize) {
//            arr[nextSeqNum] = data;
//        }
        arr[index] = data;
        return true;
    }

    public TCP_PACKET search(int index){
        return arr[index];
    }

    public int getLength(){
        return max;
    }

    public Object dequeue()
    {
        if(num==0)
        {
            return null;
        }else
        {
            Object o = arr[front];
            front=(front+1)%max;
            num--;
            return o;
        }
    }
    /**
     * 返回队列是否为空
     */
    public boolean isEmpty()
    {
        if(num==0)
            return true;
        else
            return false;
    }

    public static void main(String[] args){
        CycleQueue c = new CycleQueue(10,5);
//        c.enqueue(22);

    }
}


