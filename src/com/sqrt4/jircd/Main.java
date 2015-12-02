package com.sqrt4.jircd;

import com.sqrt4.jircd.model.Server;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server s = new Server();
        s.bind(new InetSocketAddress(6667));
        while(true) {
            Thread.sleep(1000);
        }
    }
}