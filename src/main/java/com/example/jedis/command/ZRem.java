package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class ZRem implements Command {
    private final JedisStore store;

    public ZRem(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 3) {
            RESPWriter.writeError(out, "wrong number of arguments for 'zrem'");
            return;
        }

        String key = args[1];

        SortedSet zset = store.getSortedSet(key);
        if (zset == null) {
            RESPWriter.writeInteger(out, 0);
            return;
        }

        int removed = 0;
        for (int i = 2; i < args.length; i++) {
            if (zset.remove(args[i])) {
                removed++;
            }
        }

        RESPWriter.writeInteger(out, removed);
    }

}
