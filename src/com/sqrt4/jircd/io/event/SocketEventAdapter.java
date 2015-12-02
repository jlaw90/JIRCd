package com.sqrt4.jircd.io.event;

public abstract class SocketEventAdapter implements SocketEventListener, ConnectEventListener {
    public void onRead(SocketEvent se) {
    }

    public void onWrite(SocketEvent se) {
    }

    public void onDisconnect(SocketEvent evt) {
    }

    public void onConnect(ConnectEvent evt) {
    }
}