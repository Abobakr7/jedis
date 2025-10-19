package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class LLen implements Command {
    private final JedisStore store;

    public LLen(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 2) {
            RESPWriter.writeError(out, "wrong number of arguments for 'llen'");
            return;
        }

        var list = store.getList(args[1]);
        int size = list == null ? 0 : list.size();
        RESPWriter.writeInteger(out, size);
    }
    
}
