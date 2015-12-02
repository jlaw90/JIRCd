package com.sqrt4.jircd.model.task;

import com.sqrt4.jircd.model.Client;

import java.net.InetSocketAddress;

public class ResolveHostnameTask extends ClientTask {
    public ResolveHostnameTask(Client client) {
        super(client);
    }

    public Object call() throws Exception {
        client.sendServerMessage("NOTICE", "Auth", "*** Looking up your hostname...");
        try {
            client.hostname = ((InetSocketAddress) client.getSocket().getRemoteAddress()).getHostName();
            client.sendServerMessage("NOTICE", "Auth", "*** Found your hostname (" + client.hostname + ")");
        } catch(Exception e) {
            client.sendServerMessage("NOTICE", "Auth", "*** Unable to resolve hostname, using IP");
        }
        return null;
    }
}