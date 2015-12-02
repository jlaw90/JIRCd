package com.sqrt4.jircd.model.task;

import com.sqrt4.jircd.model.Client;
import com.sqrt4.jircd.model.Server;
import com.sqrt4.jircd.model.cmd.NumericConstants;
import com.sqrt4.jircd.sched.Task;

public abstract class ClientTask extends Task implements NumericConstants {
    public final Client client;

    protected ClientTask(Client client) {
        this.client = client;
    }
}