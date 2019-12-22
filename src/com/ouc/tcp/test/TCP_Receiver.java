/***************************2.1: ACK/NACK*****************/
/***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Receiver extends TCP_Receiver_ADT {
	
	private TCP_PACKET ackPack;	//回复的ACK报文段
	private CycleQueue receQueue = new CycleQueue(100,20);	//设置窗口与滑动窗口的大小
	int seqCount = 0;
	int sequence=1;//用于记录当前待接收的包序号，注意包序号不完全是
	int count = 0;
		
	/*构造函数*/
	public TCP_Receiver() {
		super();	//调用超类构造函数
		super.initTCP_Receiver(this);	//初始化TCP接收端
	}

	@Override
	//接收到数据报：检查校验和，设置回复的ACK报文段
	public void rdt_recv(TCP_PACKET recvPack) {

		System.out.println("  Receive Packet Number: "+recvPack.getTcpH().getTh_seq());
		int index = (recvPack.getTcpH().getTh_seq() - 1) / 100 % receQueue.getLength();


		//检查校验码，生成ACK
		System.out.println("CheckSum.computeChkSum:" + CheckSum.computeChkSum(recvPack));
		System.out.println("recvPack.getTcpH().getTh_sum():" + recvPack.getTcpH().getTh_sum());
		if(CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum() ) {

			//检查是否在窗口里面
			System.out.println("base" + receQueue.getRec_base());
			System.out.println("winsize" + receQueue.getWinsize());
			System.out.println("index" + index);
			if(!receQueue.isCBwtAB(receQueue.getRec_base(), receQueue.getRec_base()+ receQueue.getWinsize(), index)) {
				System.out.println("不好意思你越窗口了，我不爱你了");

				System.out.println(" Packet Number33333333333: "+recvPack.getTcpH().getTh_seq());
				//生成ACK报文段（设置确认号）
				tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
				//回复ACK报文段
				reply(ackPack);
				return;
			}

			System.out.println(" Packet Number1111111111111: "+recvPack.getTcpH().getTh_seq());
			//生成ACK报文段（设置确认号）
			tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
			receQueue.setAcked(true, index );
			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
			//回复ACK报文段
			reply(ackPack);
			//将接收到的正确有序的数据插入data队列，准备交付
			dataQueue.add(recvPack.getTcpS().getData());


			//移动指针
			int i = receQueue.getRec_base();
			while (true) {
				if(receQueue.isAcked(i)) {
					receQueue.setAcked(false, i);
					i = (i + 1) % receQueue.getLength();
					receQueue.setRec_base(i);

				}else {
					break;
				}
			}
			System.out.println("rec_base:" + receQueue.getSend_base());

//			if(recvPack.getTcpH().getTh_seq() == sequence){
//				System.out.println(" Packet Number1111111: "+recvPack.getTcpH().getTh_seq()+" + InnerSeq:  "+sequence);
//				//生成ACK报文段（设置确认号）
////			tcpH.setTh_ack(0);
//				tcpH.setTh_ack(sequence);
//				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
//				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
//				//回复ACK报文段
//				reply(ackPack);
//				//将接收到的正确有序的数据插入data队列，准备交付
//				dataQueue.add(recvPack.getTcpS().getData());
////				sequence++;
//				sequence = sequence + 100;
//			}else if(recvPack.getTcpH().getTh_seq() < sequence){
//				tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
//				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
//				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
//				//回复ACK报文段
//				reply(ackPack);
//
//			}else {
//				tcpH.setTh_ack(sequence -100);
//				ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
//				tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
//				//回复ACK报文段
//				reply(ackPack);
//
//			}
//
		}else {

			System.out.println(" Packet Number222222222: "+recvPack.getTcpH().getTh_seq());
			tcpH.setTh_ack(-1);
//			tcpH.setTh_ack(sequence -100);
			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
			//回复ACK报文段
			reply(ackPack);

		}


		
		//交付数据（每20组数据交付一次）
		if(dataQueue.size() == 20) 
			deliver_data();	
	}

	@Override
	//交付数据（将数据写入文件）；不需要修改
	public void deliver_data() {
		//检查dataQueue，将数据写入文件
		File fw = new File("recvData.txt");
		BufferedWriter writer;
		
		
		try {
			writer = new BufferedWriter(new FileWriter(fw, true));
			
			//循环检查data队列中是否有新交付数据
			while(!dataQueue.isEmpty()) {
				int[] data = dataQueue.poll();
				
				if (count == 0 ){
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
					String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
					writer.write("start: "+date+"\n");
					
				}
				
				//将数据写入文件
				for(int i = 0; i < data.length; i++) {
					writer.write(data[i] + "\n");
				}
				count = count + data.length;
				
				if (count==100000){
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
					String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
					writer.write("end: "+date+"\n");
					
				}
					
				
				writer.flush();		//清空输出缓存
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	//回复ACK报文段,不需要修改
	public void reply(TCP_PACKET replyPack) {
		//设置错误控制标志
		tcpH.setTh_eflag((byte)1);	//eFlag=0，信道无错误
				
		//发送数据报
		client.send(replyPack);
	}
	
	
	
}
