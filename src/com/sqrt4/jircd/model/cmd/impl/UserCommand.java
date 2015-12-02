package com.sqrt4.jircd.model.cmd.impl;

import com.sqrt4.jircd.model.Client;
import com.sqrt4.jircd.model.cmd.CommandParameters;
import com.sqrt4.jircd.model.cmd.IrcCommand;
import com.sqrt4.jircd.model.cmd.NumericConstants;
import com.sqrt4.jircd.model.task.WelcomeTask;

// USER <username> <hostname> <servername> <realname> (RFC 1459)
// USER <user> <mode> <unused> <realname> (RFC 2812)
public class UserCommand extends IrcCommand implements NumericConstants {
    public int minimumParams() {
        return 4;
    }

    public void process(Client source, CommandParameters params) {
        source.username = params.poll();
        String mode = params.poll();
        String unused = params.poll();
        source.realname = params.remaining();

        new WelcomeTask(source).submit();
    }
}