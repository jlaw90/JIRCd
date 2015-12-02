package com.sqrt4.jircd.model.task;

import com.sqrt4.jircd.model.Client;
import com.sqrt4.jircd.model.Server;

public class WelcomeTask extends ClientTask {
    public WelcomeTask(Client client) {
        super(client);
    }

    public Object call() throws Exception {
        Server server = client.server;

        client.sendNumericResponse(RPL_WELCOME, String.format("Welcome to %s %s!", server.config("network_name"), client.toCanonical()));
        client.sendNumericResponse(RPL_YOURHOST, String.format("Your host is %s, running version %s", server.config("server_name"), server.config("server_version")));

        return null;
    }
}