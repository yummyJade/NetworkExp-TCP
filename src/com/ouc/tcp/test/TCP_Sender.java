/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Sender extends TCP_Sender_ADT {
	
	private TCP_PACKET tcpPack;	//待发送的TCP数据报
	int seqCount = 0;	//区分是否重复，标记为0 1 用于2.2以前
	private UDT_Timer timer;

	private CycleQueue queue = new CycleQueue(100,5);	//设置窗口与滑动窗口的大小
	private int send_base = 0;
	private int winsize = 5;
	private int nextSeqNum = 0;

	/*构造函数*/
	public TCP_Sender() {
		super();	//调用超类构造函数
		super.initTCP_Sender(this);		//初始化TCP发送端

	}
	
	@Override
	//可靠发送（应用层调用）：封装应用层数据，产生TCP数据报；需要修改
	public void rdt_send(int dataIndex, int[] appData) {
				
//		//生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
//		tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号：
//
////		tcpH.setTh_seq(seqCount);
//
//		tcpS.setData(appData);
//		tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);
//
//		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
//		tcpPack.setTcpH(tcpH);
//
//
//		//发送TCP数据报
//		udt_send(tcpPack);
//
//		//用于3.0版本：设置计时器和超时重传任务
//		timer = new UDT_Timer();
//		UDT_RetransTask reTrans = new UDT_RetransTask(client, tcpPack);
//
//		//每隔3秒执行重传，直到收到ACK
//		timer.schedule(reTrans, 3000, 3000);
//
//		//等待ACK报文
//		waitACK();


		if(queue.getNextSeqNum() < queue.getSend_base() + queue.getWinsize()) {
			make_pkt(dataIndex, appData);
			queue.enqueue( tcpPack, queue.getNextSeqNum() );
			//发送TCP数据报
			udt_send(queue.search(queue.getNextSeqNum()));
			//重置定时器的情况，即窗口重新开始
			if(queue.getSend_base() == queue.getNextSeqNum()) {
				timer = new UDT_Timer();
				Retrans reTrans = new Retrans(client, queue);
				timer.schedule(reTrans, 3000, 3000);
			}
			queue.setNextSeqNum((queue.getNextSeqNum()+1)% queue.getLength());

			//等待ACK报文
			waitACK();

		}else {
//			return;
		}




		
	}

	public void update(CycleQueue queue){
		nextSeqNum = queue.getNextSeqNum();
		send_base = queue.getSend_base();
		winsize = queue.getWinsize();
	}

	public void deupdate(CycleQueue queue, int nextSeqNum, int send_base){
		queue.setNextSeqNum(nextSeqNum );
		queue.setSend_base(send_base );
	}
	@Override
	//不可靠发送：将打包好的TCP数据报通过不可靠传输信道发送；仅需修改错误标志
	public void udt_send(TCP_PACKET stcpPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte)7);
		//System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());				
		//发送数据报
		client.send(stcpPack);
	}
	
	@Override
	//需要修改
	public void waitACK() {
		//循环检查ackQueue
		//循环检查确认号对列中是否有新收到的ACK
		while(true) {
			if(!ackQueue.isEmpty()){
				int currentAck=ackQueue.poll();
				System.out.println("CurrentAck: "+currentAck);

//				if  (currentAck == tcpPack.getTcpH().getTh_seq()){
//					System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
//					//用于3.0：
//					timer.cancel();
//					break;
//				}else{
//					System.out.println("Retransmit: "+tcpPack.getTcpH().getTh_seq());
//					udt_send(tcpPack);
//					//break;
//				}

				if  (currentAck == tcpPack.getTcpH().getTh_seq()) {
					System.out.println("Clear: " + tcpPack.getTcpH().getTh_seq());

//				send_base = ( currentAck + 1 ) % queue.getLength();
					queue.setSend_base(( (currentAck - 1 )/100 + 1) % queue.getLength());
					if (queue.getSend_base() == queue.getNextSeqNum()) {
						timer.cancel();
						break;
					} else {
						timer = new UDT_Timer();
						Retrans reTrans = new Retrans(client, queue);
						timer.schedule(reTrans, 3000, 3000);
						break;
					}
				}else {

				}



//				if  (currentAck == tcpPack.getTcpH().getTh_seq() && currentAck == 0){
//					System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
////					if(seqCount == 0){
////						seqCount = 1;
////					}else {
////						seqCount = 0;
////					}
//					System.out.println("seqCount change: "+seqCount);
//					break;
//				}else{
//					System.out.println("Retransmit: "+tcpPack.getTcpH().getTh_seq());
//					udt_send(tcpPack);
//					//break;
//				}
			}
		}
	}

	@Override
	//接收到ACK报文：检查校验和，将确认号插入ack队列;NACK的确认号为－1；3.0版本不需要修改
	public void recv(TCP_PACKET recvPack) {
		System.out.println("Receive ACK Number： "+ recvPack.getTcpH().getTh_ack());
		ackQueue.add(recvPack.getTcpH().getTh_ack());
	    System.out.println();
	}

	/**
	 * 实现校验
	 */
	public TCP_PACKET make_pkt(int dataIndex, int[] appData){
		//生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
		tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号：
//		tcpH.setTh_seq(seqCount);
		tcpS.setData(appData);
		tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);
		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
		tcpPack.setTcpH(tcpH);
		return tcpPack;
	}

}
