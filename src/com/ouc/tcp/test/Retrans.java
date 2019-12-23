package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.TimerTask;

public class Retrans extends TimerTask {

    private Client senderClient;
    private TCP_PACKET reTransPacket;
    private Integer ssthresh;
    private CycleQueue queue;
    private int type;

    public Retrans(Client client, TCP_PACKET packet, Integer ssthresh, CycleQueue queue, int type) {
        this.senderClient = client;
        this.reTransPacket = packet;
        this.ssthresh = ssthresh;
        this.queue = queue;
        this.type = type;

    }

    public void run() {

        if(type == 1){
            queue.getRoundTimer().cancel();
            queue.multiDecrease();
            this.senderClient.send(this.reTransPacket);
            System.out.println("出现超时！！！！！重置");
        }else {
            queue.addIncrease();
        }




//        int start = queue.getSend_base();
//        int end = queue.getNextSeqNum();
//        System.out.println("retrans from" + start + "to" + end + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        int i = start;
////        for (int i = start; i < end; ) {
//        while (true){
//            this.senderClient.send(queue.search(i));
//            System.out.println("重传了"+ queue.search(i).getTcpH().getTh_seq());
//            i = (i + 1) % queue.getLength();
//            if(i == end ){
//                break;
//            }
//        }
//
    }

}
