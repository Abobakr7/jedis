package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class Get implements Command {
    private final JedisStore store;

    public Get(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 2) {
            RESPWriter.writeError(out, "wrong number of arguments for 'get'");
            return;
        }

        String key = args[1];
        String value = store.get(key);
        if (value == null) {
            RESPWriter.writeNullBulkString(out);
        } else {
            RESPWriter.writeBulkString(out, value);
        }
    }
    
}
