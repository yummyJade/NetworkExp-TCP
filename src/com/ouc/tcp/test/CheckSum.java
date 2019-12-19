package com.ouc.tcp.test;

import java.util.zip.CRC32;

import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;
import sun.security.krb5.internal.crypto.crc32;

public class CheckSum {


	/*计算TCP报文段校验和：只需校验TCP首部中的seq、ack和sum，以及TCP数据字段*/
	public static short computeChkSum(TCP_PACKET tcpPack) {
		int checkSum = 0;
		
		//计算校验和
		TCP_HEADER tcpHeader = tcpPack.getTcpH();
		String combine = "";
		combine += tcpHeader.getTh_seq();
		combine += tcpHeader.getTh_ack();
		int[] data = tcpPack.getTcpS().getData();
		for(int i = 0; i < data.length; i++) {
			combine += data[i];
		}

		CRC32 crc32 = new CRC32();
		crc32.update(combine.getBytes());
		checkSum = (int)crc32.getValue();
//		short result = (short)(crc32.getValue() & 0xFFFF);
//		System.out.println("checksum is "+ result);
//		return result;
		return (short) checkSum;
	}
	
}
