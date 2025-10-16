package com.example.jedis.command;

import java.util.HashMap;

public class CommandRegistry {
    private final HashMap<String, Command> commands;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();

        commands.put("PING", new PingCommand());
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }
}
