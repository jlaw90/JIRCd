package com.sqrt4.jircd.io.event;

import com.sqrt4.jircd.io.EventedServerSocket;
import com.sqrt4.jircd.io.EventedSocket;

public class ConnectEvent {
    public final EventedServerSocket source;
    public final long time;
    public final EventedSocket socket;

    public ConnectEvent(EventedServerSocket source, long time, EventedSocket socket) {
        this.source = source;
        this.time = time;
        this.socket = socket;
    }
}