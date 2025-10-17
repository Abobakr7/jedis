package com.example.jedis.command;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class RPop implements Command {
    private final JedisStore store;

    public RPop(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 2) {
            RESPWriter.writeError(out, "wrong number of arguments for 'RPOP'");
            return;
        }

        String key = args[1];
        if (!store.containsList(key) || store.getList(key).isEmpty()) {
            RESPWriter.writeNullBulkString(out);
            return;
        }

        ConcurrentLinkedDeque<Object> list = store.getList(key);
        if (args.length > 2) {
            int count;
            try {
                count = Integer.parseInt(args[2]);
                count = Math.min(Math.max(count, 0), list.size());

                StringBuilder sb = new StringBuilder();
                sb.append("*").append(count).append("\r\n");
                
                while (count > 0) {
                    String val = (String) list.pollLast();
                    sb.append(String.format("$%d\r\n%s\r\n", val.length(), val));
                    --count;
                }
                
                out.write(sb.toString().getBytes());
            } catch (NumberFormatException e) {
                RESPWriter.writeError(out, "value is not an integer or out of range");
                return;
            }
            
        } else {
            String val = (String) list.pollLast();
            out.write(String.format("$%d\r\n%s\r\n", val.length(), val).getBytes());
        }
        out.flush();
    }
    
}
