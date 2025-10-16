package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.storage.JedisStore;

public class Set implements Command {
    private final JedisStore store;

    public Set(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length != 3 && args.length != 5) {
            out.write("-ERR wrong number of arguments for 'SET'\r\n".getBytes());
            out.flush();
            return;
        }

        String key = args[1];
        String value = args[2];
        Long expiry = null;

        if (args.length == 5) {
            String option = args[3].toUpperCase();
            String val = args[4];

            if (option.equals("EX")) {
                try {
                    expiry = System.currentTimeMillis() + Long.parseLong(val) * 1000;
                } catch (NumberFormatException e) {
                    out.write("-ERR value is not an integer or out of range\r\n".getBytes());
                    out.flush();
                    return;
                }
            } else if (option.equals("PX")) {
                try {
                    expiry = System.currentTimeMillis() + Long.parseLong(val);
                } catch (NumberFormatException e) {
                    out.write("-ERR value is not an integer or out of range\r\n".getBytes());
                    out.flush();
                    return;
                }
            } else {
                out.write("-ERR syntax error\r\n".getBytes());
                out.flush();
                return;
            }
        }

        store.set(key, value, expiry);
        out.write("+OK\r\n".getBytes());
        out.flush();
    }
    
}
