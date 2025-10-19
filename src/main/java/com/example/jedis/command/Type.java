package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class Type implements Command {
    private final JedisStore store;

    public Type(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 2) {
            RESPWriter.writeError(out, "wrong number of arguments for 'type'");
            return;
        }

        String key = args[1];
        String type;
        if (store.containsString(key)) {
            type = "string";
        } else if (store.containsList(key)) {
            type = "list";
        } else {
            type = "none";
        }

        RESPWriter.writeSimpleString(out, type);
    }
    
}
