package com.example.jedis.command;

import java.io.OutputStream;
import java.util.List;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class ZRange implements Command {
    private final JedisStore store;

    public ZRange(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 4) {
            RESPWriter.writeError(out, "wrong number of arguments for 'zrange'");
            return;
        }

        String key = args[1];
        int start, stop;
        try {
            start = Integer.parseInt(args[2]);
            stop = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            RESPWriter.writeError(out, "value is not an integer or out of range");
            return;
        }
        
        SortedSet zset = store.getSortedSet(key);
        if (zset == null) {
            out.write("*0\r\n".getBytes());
            out.flush();
            return;
        }

        List<String> members = zset.getRangeByIndex(start, stop);

        boolean withScores = args.length >= 5 && "withscores".equalsIgnoreCase(args[4]) ? true : false;
        if (withScores) {
            out.write(("*" + members.size() * 2 + "\r\n").getBytes());
            out.flush();
            for (String mem : members) {
                RESPWriter.writeBulkString(out, mem);
                String score = formatScore(zset.getScore(mem));
                RESPWriter.writeBulkString(out, score);
            }
        } else {
            out.write(("*" + members.size() + "\r\n").getBytes());
            out.flush();
            for (String mem : members) {
                RESPWriter.writeBulkString(out, mem);
            }
        }
    }

    private String formatScore(Double score) {
        if (score == null) {
            return "0";
        }
        // format the score - remove unnecessary decimal places
        if (score == score.longValue()) {
            return String.valueOf(score.longValue());
        }
        return String.valueOf(score);
    }
}
