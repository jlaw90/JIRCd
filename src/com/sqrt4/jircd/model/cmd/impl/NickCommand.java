package com.sqrt4.jircd.model.cmd.impl;

import com.sqrt4.jircd.model.Client;
import com.sqrt4.jircd.model.cmd.CommandParameters;
import com.sqrt4.jircd.model.cmd.IrcCommand;
import com.sqrt4.jircd.model.cmd.NumericConstants;

public class NickCommand extends IrcCommand implements NumericConstants {
    public int min_length = 1;
    public int max_length = 20;

    public int minimumParams() {
        return 1;
    }

    public void process(Client source, CommandParameters params) {
        if (params.size() > 1) {
            invalidNick(source);
        } else {
            String nick = params.poll();
            if (nick.length() < min_length || nick.length() > max_length)
                invalidNick(source);
            else {
                // Todo: not in use, reserved, etc.
                source.nickname = nick;
                source.sendServerMessage("NICK", source.nickname); // Todo: announce to others...
            }
        }
    }

    private void invalidNick(Client source) {
        source.sendNumericResponse(ERR_ERRONEUSNICKNAME, "NICK", "Erroneous nickname.");
    }
}