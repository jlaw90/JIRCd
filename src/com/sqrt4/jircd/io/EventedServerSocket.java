package com.sqrt4.jircd.io;

import com.sqrt4.jircd.io.event.ConnectEvent;
import com.sqrt4.jircd.io.event.ConnectEventListener;
import com.sqrt4.jircd.sched.Task;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventedServerSocket {
    private final List<ConnectEventListener> listeners = new LinkedList<ConnectEventListener>();
    private final ServerSocketChannel ssc;
    private final Selector sel;

    public EventedServerSocket() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        sel = Selector.open();
        ssc.register(sel, SelectionKey.OP_ACCEPT);
        new SelectTask().scheduleWithFixedDelay(0, 1, TimeUnit.NANOSECONDS);
    }

    public void bind(SocketAddress addr) throws IOException {
        ssc.bind(addr);
    }

    public void addConnectListener(ConnectEventListener cel) {
        synchronized (listeners) {
            listeners.add(cel);
        }
    }

    public void removeConnectListener(ConnectEventListener cel) {
        synchronized (listeners) {
            listeners.remove(cel);
        }
    }

    private class SelectTask extends Task {
        public Object call() throws Exception {
            sel.selectNow();
            Iterator<SelectionKey> it = sel.selectedKeys().iterator();

            while (it.hasNext()) {
                SelectionKey sk = it.next();
                it.remove();
                if (!sk.isValid())
                    continue;
                if (sk.isAcceptable()) {
                    EventedSocket es = new EventedSocket(ssc.accept());
                    new DispatchConnectEventTask(
                            new ConnectEvent(EventedServerSocket.this, System.currentTimeMillis(), es)
                    ).submit();
                }
            }
            return null;
        }
    }

    private class DispatchConnectEventTask extends Task {
        private ConnectEvent evt;

        public DispatchConnectEventTask(ConnectEvent evt) {
            this.evt = evt;
        }

        public Object call() throws Exception {
            ConnectEventListener[] arr;
            synchronized (listeners) {
                arr = new ConnectEventListener[listeners.size()];
                arr = listeners.toArray(arr);
            }
            for (ConnectEventListener cel : arr) {
                try {
                    cel.onConnect(evt);
                } catch (Throwable t) {
                        /**/
                }
            }
            return null;
        }
    }
}