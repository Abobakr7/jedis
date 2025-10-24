package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class ZCard implements Command {
    private final JedisStore store;

    public ZCard(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 2) {
            RESPWriter.writeError(out, "wrong number of arguments for 'zcard'");
            return;
        }

        SortedSet zset = store.getSortedSet(args[1]);
        int size = zset == null ? 0 : zset.size();
        RESPWriter.writeInteger(out, size);
    }

}
