package com.sqrt4.jircd.io;

import com.sqrt4.jircd.io.event.SocketEvent;
import com.sqrt4.jircd.io.event.SocketEventListener;
import com.sqrt4.jircd.sched.Task;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventedSocket {
    public final SocketChannel chan;
    private final List<SocketEventListener> listeners = new LinkedList<SocketEventListener>();
    private Selector sel;
    private Object att;

    public EventedSocket(SocketChannel chan) throws IOException {
        this.chan = chan;
        chan.configureBlocking(false);
        sel = Selector.open();
        chan.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        new SelectTask().scheduleWithFixedDelay(0, 1, TimeUnit.MILLISECONDS);
    }

    public int write(ByteBuffer buf) throws IOException {
        return chan.write(buf);
    }

    public long write(ByteBuffer[] srcs) throws IOException {
        return chan.write(srcs);
    }

    public long write(ByteBuffer[] srcs, int off, int len) throws IOException {
        return chan.write(srcs, off, len);
    }

    public int read(ByteBuffer dst) throws IOException {
        return chan.read(dst);
    }

    public long read(ByteBuffer[] dsts) throws IOException {
        return chan.read(dsts);
    }

    public long read(ByteBuffer[] dsts, int off, int len) throws IOException {
        return chan.read(dsts, off, len);
    }

    public SocketAddress getLocalAddress() throws IOException {
        return chan.getLocalAddress();
    }

    public SocketAddress getRemoteAddress() throws IOException {
        return chan.getRemoteAddress();
    }

    public <T> EventedSocket setOption(SocketOption<T> name, T value) throws IOException {
        chan.setOption(name, value);
        return this;
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        return chan.getOption(name);
    }

    public void attach(Object o) {
        this.att = o;
    }

    public Object attachment() {
        return att;
    }

    public void close() throws IOException {
        chan.close();
        new DispatchDisconnectEventTask(new SocketEvent(this, System.currentTimeMillis())).submit();
    }

    public void addSocketListener(SocketEventListener sel) {
        synchronized (listeners) {
            listeners.add(sel);
        }
    }

    public void removeSocketListener(SocketEventListener sel) {
        synchronized (listeners) {
            listeners.remove(sel);
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
                if (sk.isReadable()) {
                    new DispatchReadEventTask(new SocketEvent(EventedSocket.this, System.currentTimeMillis())).submit();
                }
                if (sk.isWritable()) {
                    new DispatchWriteEventTask(new SocketEvent(EventedSocket.this, System.currentTimeMillis())).submit();
                }
            }
            return null;
        }
    }

    private class DispatchReadEventTask extends Task {
        private SocketEvent evt;

        public DispatchReadEventTask(SocketEvent evt) {
            this.evt = evt;
        }

        public Object call() throws Exception {
            SocketEventListener[] arr;
            synchronized (listeners) {
                arr = new SocketEventListener[listeners.size()];
                arr = listeners.toArray(arr);
            }
            try {
                for (SocketEventListener sel : arr) {
                    sel.onRead(evt);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                evt.source.close();
            }
            return null;
        }
    }

    private class DispatchWriteEventTask extends Task {
        private SocketEvent evt;

        public DispatchWriteEventTask(SocketEvent evt) {
            this.evt = evt;
        }

        public Object call() throws Exception {
            SocketEventListener[] arr;
            synchronized (listeners) {
                arr = new SocketEventListener[listeners.size()];
                arr = listeners.toArray(arr);
            }
            try {
                for (SocketEventListener sel : arr) {
                    sel.onWrite(evt);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                evt.source.close();
            }
            return null;
        }
    }

    private class DispatchDisconnectEventTask extends Task {
        private SocketEvent evt;

        public DispatchDisconnectEventTask(SocketEvent evt) {
            this.evt = evt;
        }

        public Object call() throws Exception {
            SocketEventListener[] arr;
            synchronized (listeners) {
                arr = new SocketEventListener[listeners.size()];
                arr = listeners.toArray(arr);
            }
            for (SocketEventListener sel : arr) {
                try {
                    sel.onDisconnect(evt);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return null;
        }
    }
}