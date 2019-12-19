package com.ouc.tcp.test;

import java.util.zip.CRC32;

public class test {
    public static void main(){
        CRC32 crc32 = new CRC32();
        crc32.update("abcdfg".getBytes());
        crc32.update("abcdfg".getBytes());
        System.out.println(crc32.getValue());
    }
}
