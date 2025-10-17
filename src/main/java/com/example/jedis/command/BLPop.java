package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;

public class BLPop implements Command {
    private final JedisStore store;

    public BLPop(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 3) {
            RESPWriter.writeError(out, "wrong number of arguments for 'BLPOP'");
            return;
        }

        double timeoutSeconds;
        try {
            timeoutSeconds = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            RESPWriter.writeError(out, "timeout is not a float or out of range");
            return;
        }

        if (timeoutSeconds < 0) {
            RESPWriter.writeError(out, "timeout cannot be negative");
            return;
        }

        long timeoutMillis = timeoutSeconds == 0 ? Long.MAX_VALUE : (long) (timeoutSeconds * 1000);
        long startTime = System.currentTimeMillis();
        String key = args[1];

        // while loop to try to pop until timeout
        while (true) {
            var list = store.getList(key);

            if (list != null && !list.isEmpty()) {
                String value = (String) list.pollFirst();
                if (value != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("*2\r\n");;
                    sb.append("$").append(key.length()).append("\r\n");
                    sb.append(key).append("\r\n");
                    sb.append("$").append(value.length()).append("\r\n");
                    sb.append(value).append("\r\n");

                    out.write(sb.toString().getBytes());
                    out.flush();
                    return;
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMillis) {
                out.write("*-1\r\n".getBytes());
                out.flush();
                return;
            }

            // sleep for a short interval before checking again (polling)
            Thread.sleep(Math.min(100, timeoutMillis - elapsed));
        }
    }
    
}
