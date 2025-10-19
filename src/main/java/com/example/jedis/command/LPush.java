package com.example.jedis.command;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class LPush implements Command {
    private final JedisStore store;

    public LPush(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 3) {
            RESPWriter.writeError(out, "wrong number of arguments for 'lpush'");
            return;
        }

        store.putListIfAbsent(args[1], new ConcurrentLinkedDeque<>());
        ConcurrentLinkedDeque<Object> list = store.getList(args[1]);
        for (int i = 2; i < args.length; ++i) {
            list.addFirst(args[i]);
        }

        RESPWriter.writeInteger(out, list.size());
    }
    
}
