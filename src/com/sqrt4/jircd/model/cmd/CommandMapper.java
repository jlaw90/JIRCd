package com.sqrt4.jircd.model.cmd;

import java.util.HashMap;
import java.util.Map;

public class CommandMapper {
    public Map<String, IrcCommand> processors = new HashMap<String, IrcCommand>();

    public void register(String command, IrcCommand processor) {
        processors.put(command.toLowerCase(), processor);
    }

    public IrcCommand getProcessor(String command) {
        return processors.get(command.toLowerCase());
    }
}