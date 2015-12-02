package com.sqrt4.jircd.model.cmd;

import com.sqrt4.jircd.model.Client;

public abstract class IrcCommand {
    public int minimumParams() {
        return 0;
    }

    public abstract void process(Client source, CommandParameters params);
}