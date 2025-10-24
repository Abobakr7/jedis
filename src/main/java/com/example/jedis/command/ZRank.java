package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class ZRank implements Command {
    private final JedisStore store;

    public ZRank(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length != 3) {
            RESPWriter.writeError(out, "wrong number of arguments for 'ZRANK'");
            return;
        }

        String key = args[1], member = args[2];

        if (!store.containsSortedSet(key)) {
            RESPWriter.writeNullBulkString(out);
            return;
        }

        SortedSet zset = store.getSortedSet(key);
        Integer rank = zset.getRank(member);

        if (rank == null) {
            RESPWriter.writeNullBulkString(out);
        } else {
            RESPWriter.writeInteger(out, rank);
        }
    }

}
