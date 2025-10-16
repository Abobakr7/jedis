package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.storage.JedisStore;

public class Get implements Command {
    private final JedisStore store;

    public Get(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 2) {
            out.write("-ERR wrong number of arguments for 'GET'\r\n".getBytes());
            out.flush();
            return;
        }

        String key = args[1];
        String value = store.get(key);
        if (value == null) {
            out.write("$-1\r\n".getBytes());
        } else {
            out.write(String.format("$%d\r\n%s\r\n", value.length(), value).getBytes());
        }
        out.flush();
    }
    
}
