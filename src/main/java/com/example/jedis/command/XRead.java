package com.example.jedis.command;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.StreamEntry;

public class XRead implements Command {
    private final JedisStore store;

    public XRead(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 4) {
            RESPWriter.writeError(out, "wrong number of arguments for 'xread'");
            return;
        }
        
        int offset = 1;
        long blockMillis = -1; // -1 for no blocking
        
        if (args[offset].equalsIgnoreCase("block")) {
            if (args.length < 6) {
                RESPWriter.writeError(out, "wrong number of arguments for 'xread'");
                return;
            }
            try {
                blockMillis = Long.parseLong(args[offset + 1]);
            } catch (NumberFormatException e) {
                RESPWriter.writeError(out, "invalid block timeout");
                return;
            }
            offset += 2;
        }

        if (!args[offset].equalsIgnoreCase("streams")) {
            RESPWriter.writeError(out, "syntax error");
            return;
        }
        offset++;

        int remainArgs = args.length - offset;
        if (remainArgs % 2 != 0 || remainArgs == 0) {
            RESPWriter.writeError(out, "wrong number of arguments for 'xread'");
            return;
        }

        int streamCount = remainArgs / 2;
        String[] keys = new String[streamCount];
        String[] startIds = new String[streamCount];

        // parse keys
        for (int i = 0; i < streamCount; i++) {
            keys[i] = args[offset + i];
        }
        // parse IDs
        for (int i = 0; i < streamCount; i++) {
            startIds[i] = args[offset + streamCount + i];
        }

        if (blockMillis >= 0) {
            executeBlocking(keys, startIds, blockMillis, out);
        } else {
            executeNonBlocking(keys, startIds, out);
        }
    }

    private void executeNonBlocking(String[] keys, String[] startIds, OutputStream out) throws Exception {
        List<StreamResult> results = new ArrayList<>();

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String startId = startIds[i];
            
            List<StreamEntry> stream = store.getStream(key);
            if (stream == null || stream.isEmpty()) {
                continue;
            }

            List<StreamEntry> matchingEntries = getEntriesAfter(stream, startId);
            if (!matchingEntries.isEmpty()) {
                results.add(new StreamResult(key, matchingEntries));
            }
        }

        writeResponse(results, out);
    }

    private void executeBlocking(String[] keys, String[] startIds, long blockMillis, OutputStream out) throws Exception {
        long startTime = System.currentTimeMillis();
        long timeout = blockMillis == 0 ? Long.MAX_VALUE : blockMillis;

        // handle $ special case - resolve it once before entering the loop
        String[] resolvedIds = new String[startIds.length];
        for (int i = 0; i < startIds.length; i++) {
            if (startIds[i].equals("$")) {
                List<StreamEntry> stream = store.getStream(keys[i]);
                if (stream != null && !stream.isEmpty()) {
                    resolvedIds[i] = stream.get(stream.size() - 1).getId();
                } else {
                    resolvedIds[i] = "0-0";
                }
            } else {
                resolvedIds[i] = startIds[i];
            }
        }

        while (true) {
            List<StreamResult> results = new ArrayList<>();

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                String startId = resolvedIds[i];
                
                List<StreamEntry> stream = store.getStream(key);
                if (stream != null && !stream.isEmpty()) {
                    List<StreamEntry> matchingEntries = getEntriesAfter(stream, startId);
                    if (!matchingEntries.isEmpty()) {
                        results.add(new StreamResult(key, matchingEntries));
                    }
                }
            }

            if (!results.isEmpty()) {
                writeResponse(results, out);
                return;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeout) {
                out.write("*-1\r\n".getBytes());
                out.flush();
                return;
            }

            // sleep briefly before checking again
            Thread.sleep(Math.min(100, timeout - elapsed));
        }
    }

    private List<StreamEntry> getEntriesAfter(List<StreamEntry> stream, String startId) {
        List<StreamEntry> result = new ArrayList<>();
        for (StreamEntry entry : stream) {
            if (compareIds(entry.getId(), startId) > 0) {
                result.add(entry);
            }
        }
        return result;
    }

    private void writeResponse(List<StreamResult> results, OutputStream out) throws Exception {
        if (results.isEmpty()) {
            out.write("*-1\r\n".getBytes());
            out.flush();
            return;
        }

        StringBuilder response = new StringBuilder();
        response.append("*").append(results.size()).append("\r\n");

        for (StreamResult result : results) {
            response.append("*2\r\n");
            
            response.append("$").append(result.key.length()).append("\r\n");
            response.append(result.key).append("\r\n");
            
            response.append("*").append(result.entries.size()).append("\r\n");            
            for (StreamEntry entry : result.entries) {
                response.append("*2\r\n");
                
                String id = entry.getId();
                response.append("$").append(id.length()).append("\r\n");
                response.append(id).append("\r\n");
                
                Map<String, String> fields = entry.getFields();
                response.append("*").append(fields.size() * 2).append("\r\n");
                
                for (Map.Entry<String, String> field : fields.entrySet()) {
                    String fieldName = field.getKey();
                    String fieldValue = field.getValue();
                    
                    response.append("$").append(fieldName.length()).append("\r\n");
                    response.append(fieldName).append("\r\n");
                    response.append("$").append(fieldValue.length()).append("\r\n");
                    response.append(fieldValue).append("\r\n");
                }
            }
        }
        out.write(response.toString().getBytes());
        out.flush();
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

    private static class StreamResult {
        String key;
        List<StreamEntry> entries;

        StreamResult(String key, List<StreamEntry> entries) {
            this.key = key;
            this.entries = entries;
        }
    }
}
