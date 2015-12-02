package com.sqrt4.jircd.model;

import com.sqrt4.jircd.io.EventedSocket;
import com.sqrt4.jircd.io.event.SocketEvent;
import com.sqrt4.jircd.io.event.SocketEventListener;
import com.sqrt4.jircd.model.cmd.CommandParameters;
import com.sqrt4.jircd.model.cmd.IrcCommand;
import com.sqrt4.jircd.model.cmd.NumericConstants;
import com.sqrt4.jircd.model.task.ResolveHostnameTask;
import com.sqrt4.jircd.pool.impl.ByteBufferPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Client implements SocketEventListener {
    public String username, hostname, servername, realname, nickname;

    public final Server server;
    private EventedSocket sock;
    private final ByteBuffer in = ByteBufferPool.acquire(8192);
    private final Queue<ByteBuffer> out = new LinkedList<ByteBuffer>();

    public Client(Server server, EventedSocket socket) {
        this.server = server;
        this.sock = socket;
        try {
            this.hostname = ((InetSocketAddress) socket.getRemoteAddress()).getHostString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sock.addSocketListener(this);

        // Schedule a task to do a hostname lookup
        try {
            new ResolveHostnameTask(this).submit().get();
        } catch (InterruptedException e) {
            /**/
        } catch (ExecutionException e) {
            /**/
        }
    }

    private void processLine(String line) {
        System.out.println(">> " + line);
        int idx = line.indexOf(' ');
        String cmd, params;
        if (idx != -1) {
            cmd = line.substring(0, idx);
            params = line.substring(idx + 1);
        } else {
            cmd = line;
            params = "";
        }

        cmd = cmd.toLowerCase();

        IrcCommand proc = server.commandMapper.getProcessor(cmd);
        if (proc == null)
            System.err.println("Unrecognised command: " + cmd);
        else {
            CommandParameters cp = new CommandParameters(params);
            if(cp.size() < proc.minimumParams()) {
                sendNumericResponse(NumericConstants.ERR_NEEDMOREPARAMS, cmd.toUpperCase(), "Not enough parameters.");
            } else {
                proc.process(this, cp);
            }
        }
    }

    private void processInputBuffer() {
        for (int i = 0; i < in.position(); i++) {
            byte b = in.get(i);
            if (b == '\n') { // LINE\r?\n
                int rem = 1;
                if (i >= 1 && in.get(i - 1) == '\r')
                    rem = 2;
                // We have a line
                StringBuilder sb = new StringBuilder(i);
                for (int j = 0; j <= (i - rem); j++)
                    sb.append((char) in.get(j));

                i += 1;

                // Reset
                for (int j = i; j < in.position(); j++) // Move remaining bytes to the beginning of the buffer
                    in.put(j - i, in.get(j));
                in.position(in.position() - i); // Move the position
                i = -1; // Set to -1, so on next iteration it will be 0

                processLine(sb.toString());
            }
        }
    }

    public EventedSocket getSocket() {
        return sock;
    }

    public void onRead(SocketEvent se) {
        synchronized (in) {
            try {
                se.source.read(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            processInputBuffer();
        }
    }

    public void onWrite(SocketEvent se) {
        ByteBuffer cur;
        synchronized (out) {
            cur = out.peek();
            if (cur == null)
                return;
            synchronized (cur) { // synchronise on the packet, in case another thread tries writing it at the same time
                if (cur.position() == 0) {
                    System.out.print("<< ");
                    for (int i = 0; i < cur.limit(); i++) {
                        char c = (char) cur.get(i);
                        System.out.print(c);
                    }
                }
                try {
                    se.source.write(cur);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (cur.remaining() == 0) {
                    out.poll();

                }
            }
        }
    }

    public void onDisconnect(SocketEvent evt) {
        System.out.println("Disconnect");
    }

    public void write(ByteBuffer bb) {
        bb.flip();
        synchronized (out) {
            out.add(bb);
        }
    }

    public void sendMessage(String prefix, String command, String... params) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null)
            sb.append(':').append(prefix).append(' ');

        sb.append(command);

        for (int i = 0; i < params.length - 1; i++)
            sb.append(' ').append(params[i]);
        if (params.length - 1 >= 0)
            sb.append(" :").append(params[params.length - 1]);

        sb.append("\r\n");

        ByteBuffer bb = ByteBufferPool.acquire(sb.length());
        for (char c : sb.toString().toCharArray())
            bb.put((byte) c);
        write(bb);
    }

    public void sendServerMessage(String command, String... params) {
        sendMessage(server.config("hostname"), command, params);
    }

    public void sendUserMessage(String command, String... params) {
        sendMessage(nickname, command, params);
    }

    public void sendNumericResponse(int code, String... params) {
        sendServerMessage(String.format("%03d %s", code, nickname), params);
    }

    public String toCanonical() {
        return String.format("%s!%s@%s", nickname, username, hostname);
    }
}