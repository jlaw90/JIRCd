package com.sqrt4.jircd.model;

import com.sqrt4.jircd.io.EventedServerSocket;
import com.sqrt4.jircd.io.event.*;
import com.sqrt4.jircd.model.cmd.CommandMapper;
import com.sqrt4.jircd.model.cmd.impl.NickCommand;
import com.sqrt4.jircd.model.cmd.impl.UserCommand;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.LinkedList;

public class Server implements ConnectEventListener {
    public final LinkedList<Client> clients = new LinkedList<Client>();
    public final CommandMapper commandMapper = new CommandMapper();
    private EventedServerSocket sock;

    public Server() throws IOException {
        sock = new EventedServerSocket();
        sock.addConnectListener(this);

        registerDefaultCommands();
    }

    public void bind(SocketAddress addr) throws IOException {
        sock.bind(addr);
    }

    public void onConnect(ConnectEvent evt) {
        evt.socket.addSocketListener(new SocketEventAdapter() {
            public void onDisconnect(SocketEvent evt) {
                synchronized (clients) {
                    clients.remove(evt.source.attachment());
                }
            }
        });
        Client c = new Client(this, evt.socket);
        evt.socket.attach(c);
        synchronized (clients) {
            clients.add(c);
        }
    }

    private void registerDefaultCommands() {
        commandMapper.register("NICK", new NickCommand());
        commandMapper.register("USER", new UserCommand());
    }

    public String config(String key) {
        // Todo: use config...
        return key;
    }
}