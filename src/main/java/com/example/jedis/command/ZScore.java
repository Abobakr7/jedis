package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class ZScore implements Command {
    private final JedisStore store;

    public ZScore(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 3) {
            RESPWriter.writeError(out, "wrong number of arguments for 'zscore'");
            return;
        }

        String key = args[1], member = args[2];

        SortedSet zset = store.getSortedSet(key);
        if (zset == null) {
            RESPWriter.writeNullBulkString(out);
            return;
        }

        Double score = zset.getScore(member);
        if (score == null) {
            RESPWriter.writeNullBulkString(out);
            return;
        }

        RESPWriter.writeBulkString(out, formatScore(score));
    }

    private String formatScore(Double score) {
        // format the score - remove unnecessary decimal places
        if (score == score.longValue()) {
            return String.valueOf(score.longValue());
        }
        return String.valueOf(score);
    }
}
