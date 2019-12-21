package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.TimerTask;

public class Retrans extends TimerTask {

    private Client senderClient;
//    private TCP_PACKET reTransPacket;
    private CycleQueue queue;

    public Retrans(Client client, CycleQueue queue) {
        this.senderClient = client;
//        this.reTransPacket = packet;
        this.queue = queue;

    }

    public void run() {
//        this.senderClient.send(this.reTransPacket);
        int start = queue.getSend_base();
        int end = queue.getNextSeqNum();
        System.out.println("retrans from" + start + "to" + end + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        int i = start;
//        for (int i = start; i < end; ) {
        while (true){
            this.senderClient.send(queue.search(i));
            System.out.println("重传了"+ queue.search(i).getTcpH().getTh_seq());
            i = (i + 1) % queue.getLength();
            if(i == end ){
                break;
            }
        }

    }

}
