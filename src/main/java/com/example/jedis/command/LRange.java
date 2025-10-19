package com.example.jedis.command;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class LRange implements Command {
    private final JedisStore store;

    public LRange(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 4) {
            RESPWriter.writeError(out, "wrong number of arguments for 'lrange'");
            return;
        }

        String key = args[1];
        if (!store.containsList(key) || store.getList(key).isEmpty()) {
            RESPWriter.writeEmptyArray(out);
            return;
        }

        // take snapshot first for thread safety
        ConcurrentLinkedDeque<Object> list = store.getList(key);
        Object[] listArray = list.toArray(new Object[0]);
        int size = listArray.length;

        int start, end;
        try {
            start = Integer.parseInt(args[2]);
            end = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            RESPWriter.writeError(out, "value is not an integer or out of range");
            return;
        }

        // handle negative indices
        if (start < 0) start += size;
        if (end < 0) end += size;

        // clamp boundaries
        if (start < 0) start = 0;
        if (end >= size) end = size - 1;

        if (start > end || start >= size) {
            RESPWriter.writeEmptyArray(out);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(end - start + 1).append("\r\n");

        for (int i = start; i <= end; i++) {
            String value = listArray[i].toString();
            sb.append("$").append(value.length()).append("\r\n")
                .append(value).append("\r\n");
        }
        out.write(sb.toString().getBytes());
        out.flush();
    }
    
}
