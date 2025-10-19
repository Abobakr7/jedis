package com.example.jedis.command;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.StreamEntry;

public class XRange implements Command {
    private final JedisStore store;

    public XRange(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 4) {
            RESPWriter.writeError(out, "wrong number of arguments for 'xrange'");
            return;
        }

        String key = args[1];
        String startId = args[2];
        String endId = args[3];

        List<StreamEntry> stream = store.getStream(key);
        if (stream == null || stream.isEmpty()) {
            RESPWriter.writeEmptyArray(out);
            return;
        }

        // special cases '-' and '+'
        if (startId.equals("-")) {
            startId = "0-0";
        }
        if (endId.equals("+")) {
            endId = Long.MAX_VALUE + "-" + Integer.MAX_VALUE;
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (StreamEntry entry : stream) {
            String id = entry.getId();
            if (isInRange(id, startId, endId)) {
                sb.append("*2\r\n");

                sb.append("$").append(id.length()).append("\r\n");
                sb.append(id).append("\r\n");

                Map<String, String> fields = entry.getFields();
                sb.append("*").append(fields.size() * 2).append("\r\n");

                for (Map.Entry<String, String> field : fields.entrySet()) {
                    String fieldName = field.getKey();
                    String fieldValue = field.getValue();

                    sb.append("$").append(fieldName.length()).append("\r\n");
                    sb.append(fieldName).append("\r\n");
                    sb.append("$").append(fieldValue.length()).append("\r\n");
                    sb.append(fieldValue).append("\r\n");
                }

                count++;
            }
        }

        StringBuilder res = new StringBuilder();
        res.append("*").append(count).append("\r\n");
        res.append(sb.toString());

        out.write(res.toString().getBytes());
        out.flush();
    }

    private boolean isInRange(String id, String startId, String endId) {
        return compareIds(id, startId) >= 0 && compareIds(id, endId) <= 0;
    }

    private int compareIds(String id1, String id2) {
        String[] parts1 = id1.split("-");
        String[] parts2 = id2.split("-");
        
        long millis1 = Long.parseLong(parts1[0]);
        long millis2 = Long.parseLong(parts2[0]);
        
        if (millis1 != millis2) {
            return Long.compare(millis1, millis2);
        }
        
        int seq1 = Integer.parseInt(parts1[1]);
        int seq2 = Integer.parseInt(parts2[1]);
        
        return Integer.compare(seq1, seq2);
    }
}
