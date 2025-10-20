package com.example.jedis.command;

import java.util.HashMap;

import com.example.jedis.storage.JedisStore;

public class CommandRegistry {
    private final HashMap<String, Command> commands;
    private final JedisStore store;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
        this.store = new JedisStore();

        commands.put("ping", new Ping());
        commands.put("echo", new Echo());

        commands.put("set", new Set(store));
        commands.put("get", new Get(store));
        commands.put("rpush", new RPush(store));
        commands.put("lpush", new LPush(store));
        commands.put("lrange", new LRange(store));
        commands.put("llen", new LLen(store));
        commands.put("lpop", new LPop(store));
        commands.put("rpop", new RPop(store));
        commands.put("blpop", new BLPop(store));
        commands.put("type", new Type(store));
        commands.put("xadd", new XAdd(store));
        commands.put("xrange", new XRange(store));
        commands.put("xread", new XRead(store));
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }
}
