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

	private CycleQueue queue = new CycleQueue(1000,1, 16);	//设置窗口与滑动窗口的大小
	private int send_base = 0;
	private int winsize = 5;
	private int nextSeqNum = 0;
//	private Integer ssthresh = 16;	//拥塞门限
	private long RTT = 0;		//往返时延
	private int roundCount; 	//传输轮次计算
	private UDT_Timer roundTimer;		//传输轮次计时器

//	private Date date; 	//

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



//			make_pkt(dataIndex, appData);

		if(queue.getNoAckLength() >= queue.getWinsize()) {
			System.out.println("接下来的"+tcpPack.getTcpH().getTh_seq()+"以后的包就先不要发了");
			waitSEND();
		}

		if(queue.getNoAckLength() < queue.getWinsize()){
			tcpH = new TCP_HEADER();
			tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号
//		tcpH.setTh_seq(seqCount);
			tcpS.setData(appData);
			tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);
			tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
			tcpPack.setTcpH(tcpH);
			try{
				queue.enqueue( tcpPack.clone(), queue.getNextSeqNum() );
			}catch (Exception e){
				System.out.println(e);
			}

			System.out.println("发送了"+ queue.search(queue.getNextSeqNum()).getTcpH().getTh_seq()+ "在"+queue.getNextSeqNum());
			//发送TCP数据报
			udt_send(queue.search(queue.getNextSeqNum()));
			//重置定时器的情况，即窗口重新开始
//			if(queue.getSend_base() == queue.getNextSeqNum()) {
				UDT_Timer timer = new UDT_Timer();
			try{
				Retrans reTrans = new Retrans(client, tcpPack.clone(), queue.getSsthresh(), queue, 1);
				timer.schedule(reTrans, 2000, 2000);
			}catch (Exception e){
				System.out.println(e);
			}
				queue.setTimer(timer, queue.getNextSeqNum());
			// 设置发送时间戳
			queue.setSend_time(new Date().getTime());
//			}
			queue.setNextSeqNum((queue.getNextSeqNum()+1)% queue.getLength());
			//清除一切可能
			queue.setAcked(false,(queue.getNextSeqNum()+1)% queue.getLength());

			//等待ACK报文
//			waitACK();
			System.out.println("Send_base3(: " + queue.getSend_base());
			System.out.println("nextNum 3(: " + queue.getNextSeqNum());


//			if(queue.getNoAckLength() == queue.getWinsize() - 1){
//				System.out.println("接下来的"+tcpPack.getTcpH().getTh_seq()+"以后的包就先不要发了");
//				waitSEND();
//			}

		}else{
			System.out.println("!!!!超过了");

			return;
		}




		
	}

	@Override
	//不可靠发送：将打包好的TCP数据报通过不可靠传输信道发送；仅需修改错误标志
	public void udt_send(TCP_PACKET stcpPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte)3);
		stcpPack.getTcpH().setTh_eflag((byte)7);
		//System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());				
		//发送数据报
		client.send(stcpPack);
	}

	//用于卡死发送程序
	public void waitSEND(){

		while(true){
			if(queue.getNextSeqNum() == queue.getSend_base() ) {
				System.out.println("现在你被释放了，你可以继续发送了");
				break;
			}
		}

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

				if  (currentAck == tcpPack.getTcpH().getTh_seq()){
					System.out.println("Clear: "+tcpPack.getTcpH().getTh_seq());
					//用于3.0：
					timer.cancel();
					break;
				}else{
					System.out.println("Retransmit: "+tcpPack.getTcpH().getTh_seq());
					udt_send(tcpPack);
					//break;
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
//		ackQueue.add(recvPack.getTcpH().getTh_ack());
		int index = ( (recvPack.getTcpH().getTh_ack() - 1 )/100 ) % queue.getLength();
		if  (recvPack.getTcpH().getTh_ack() > 0) {

//			if(recvPack.getTcpH().getTh_ack() >= queue.getSend_base())
			System.out.println("Clear: " + recvPack.getTcpH().getTh_ack());

//			queue.setSend_base(index);


			//超出门限，开始进入拥塞控制阶段
			if( queue.getWinsize() > queue.getSsthresh()){
				roundTimer = new UDT_Timer();
				try{
					Retrans reTrans = new Retrans(client, tcpPack.clone(), queue.getSsthresh(), queue, 2);
					roundTimer.schedule(reTrans,RTT);
					queue.setRoundTimer(roundTimer);		//设置对列里

				}catch (Exception e){
					System.out.println(e);

				}

				System.out.println("开始进入拥塞控制状态");
			}

			//统计轮次，更新RTT
			if( roundCount > queue.getWinsize()) {
				roundCount = 0;
//			RTT = queue.getTimer(index).;
				long nowTime = new Date().getTime();
				long inter = nowTime - queue.getSend_time();
				if(inter < 4000) {
					RTT = (long)(RTT * (1-0.125) + inter * 0.125);
				}
				System.out.println("RTT时间为:"+RTT);

			}else {
				roundCount++;
			}
			System.out.println("winSize:"+roundCount);
			System.out.println("roundCount:"+roundCount);
			queue.getTimer(index).cancel();
			queue.setAcked(true, index);

			//移动指针
			int i = queue.getSend_base();
			while (true) {
				if(queue.isAcked(i)) {
					queue.setAcked(false, i);
					i = (i + 1) % queue.getLength();
					queue.setSend_base(i);


				}else {
					break;
				}
			}


			System.out.println("Send_base1(: " + queue.getSend_base());
			System.out.println("next ssss1(: " + queue.getNextSeqNum());


				//每收到一次确认报文，拥塞窗口加一(收到确认以后send_base一定可以后移)
			if( queue.getWinsize() <= queue.getSsthresh()){
				queue.setWinsize(queue.getWinsize() + 1);
			}



			System.out.println("当前发送窗口为:" + queue.getWinsize());
			System.out.println("当前门限为："+ queue.getSsthresh());

//			if (queue.getSend_base() == queue.getNextSeqNum()) {
//				timer.cancel();
//
//			} else {
//
//				System.out.println("refresh the timer");
//				System.out.println("re Send_base(: " + queue.getSend_base());
//				System.out.println("re next  ssss(: " + queue.getNextSeqNum());
//				timer = new UDT_Timer();
//				Retrans reTrans = new Retrans(client, tcpPack);
//				timer.schedule(reTrans, 3000, 3000);
//			}



		}
		else if(recvPack.getTcpH().getTh_ack() == -1){
////			//报文错误，重传
//////			System.out.println("Retransmit: "+tcpPack.getTcpH().getTh_seq());
//			for (int i = queue.send_base; i < queue.getNextSeqNum(); i = (i + 1) % queue.getLength()) {
//				udt_send(queue.search(recvPack.getTcpH().getTh_seq()));
////			}
//			UDT_Timer timer = new UDT_Timer();
//			Retrans retrans = new Retrans(client, recvPack);
//			timer.schedule(retrans, 3000, 3000);
//			System.out.println("Send_base2(: " + queue.getSend_base());
//			System.out.println("next  ssss2(: " + queue.getNextSeqNum());

		}else if(recvPack.getTcpH().getTh_ack() < 0){
//			udt_send(queue.search(recvPack.getTcpH().getTh_seq()));
//			UDT_Timer timer = new UDT_Timer();
//			Retrans retrans = new Retrans(client, recvPack);
//			timer.schedule(retrans, 3000, 3000);
//			System.out.println("Send_base3(: " + queue.getSend_base());
//			System.out.println("next  ssss3(: " + queue.getNextSeqNum());
		}

	}

	/**
	 * 实现校验
	 */
	public TCP_PACKET make_pkt(int dataIndex, int[] appData){
		//生成TCP数据报（设置序号和数据字段/校验和),注意打包的顺序
//		tcpH.setTh_seq(dataIndex * appData.length + 1);//包序号设置为字节流号：
//		System.out.println("打包");
////		tcpH.setTh_seq(seqCount);
//		tcpS.setData(appData);
//		tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);
//		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
//		tcpPack.setTcpH(tcpH);
		return tcpPack;
	}

}
