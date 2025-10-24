package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class ZAdd implements Command {
    private final JedisStore store;

    public ZAdd(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 4) {
            RESPWriter.writeError(out, "wrong number of arguments for 'zadd'");
            return;
        }

        String key = args[1];
        SortedSet zset = store.getOrCreateSortedSet(key);
        int added = 0;
        
        for (int i = 2; i < args.length; i += 2) {
            try {
                double score = Double.parseDouble(args[i]);
                if (i + 1 >= args.length) {
                    RESPWriter.writeError(out, "wrong number of arguments for 'zadd'");
                    return;
                }
    
                String member = args[i + 1];
                if (zset.add(member, score)) {
                    added++;
                }
            } catch (NumberFormatException e) {
                RESPWriter.writeError(out, "value is not a valid float");
            }
        }

        RESPWriter.writeInteger(out, added);
    }

}
