package com.example.jedis.command;

import java.util.HashMap;

import com.example.jedis.storage.JedisStore;

public class CommandRegistry {
    private final HashMap<String, Command> commands;
    private final JedisStore store;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
        this.store = new JedisStore();

        commands.put("PING", new PingCommand());
        commands.put("ECHO", new Echo());

        commands.put("SET", new Set(store));
        commands.put("GET", new Get(store));
        commands.put("RPUSH", new RPush(store));
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }
}
