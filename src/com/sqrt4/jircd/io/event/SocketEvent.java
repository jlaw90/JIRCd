package com.sqrt4.jircd.io.event;

import com.sqrt4.jircd.io.EventedSocket;

public class SocketEvent {
    public final EventedSocket source;
    public final long time;

    public SocketEvent(EventedSocket source, long time) {
        this.source = source;
        this.time = time;
    }
}