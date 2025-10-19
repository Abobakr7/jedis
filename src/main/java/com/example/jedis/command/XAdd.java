package com.example.jedis.command;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.StreamEntry;

public class XAdd implements Command {
    private final JedisStore store;

    public XAdd(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 5 || args.length % 2 != 1) {
            RESPWriter.writeError(out, "wrong number of arguments for 'xadd'");
            return;
        }

        String key = args[1];
        String id = args[2];
        Map<String, String> fields = new HashMap<>();
        for (int i = 3; i < args.length; i += 2) {
            fields.put(args[i], args[i + 1]);
        }

        String generatedId = store.addStreamEntry(key, id, new StreamEntry(id, fields));
        if (generatedId == null) {
            RESPWriter.writeError(out, "The ID specified in 'xadd' is equal or smaller than the target stream top item");
            return;
        }

        if (generatedId.equals("ERR_ZERO_ZERO")) {
            RESPWriter.writeError(out, "The ID specified in 'xadd' must be greater than 0-0");
            return;
        }

        RESPWriter.writeBulkString(out, generatedId);
    }
    
}
