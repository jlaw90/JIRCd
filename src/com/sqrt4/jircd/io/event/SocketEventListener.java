package com.sqrt4.jircd.io.event;

public interface SocketEventListener {
    void onRead(SocketEvent se);

    void onWrite(SocketEvent se);

    void onDisconnect(SocketEvent evt);
}