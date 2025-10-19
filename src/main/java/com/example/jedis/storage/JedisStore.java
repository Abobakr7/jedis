package com.example.jedis.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class JedisStore {
    private final ConcurrentHashMap<String, ValueEntry> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Object>> listStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<StreamEntry>> streamStore = new ConcurrentHashMap<>();


    // normal store
    public void set(String key, String value, Long expiry) {
        store.put(key, new ValueEntry(value, expiry));
    }

    public String get(String key) {
        ValueEntry val = store.get(key);
        if (val == null) {
            return null;
        }

        if (val.getExpiry() != null && System.currentTimeMillis() > val.getExpiry()) {
            store.remove(key);
            return null;
        }

        return (String) val.getValue();
    }

    public boolean containsString(String key) {
        return store.containsKey(key);
    }

    // lists
    public ConcurrentLinkedDeque<Object> getList(String key) {
        return listStore.get(key);
    }

    public void putListIfAbsent(String key, ConcurrentLinkedDeque<Object> list) {
        listStore.putIfAbsent(key, list);
    }

    public boolean containsList(String key) {
        return listStore.containsKey(key);
    }

    // streams
    public void putStreamIfAbsent(String key, List<StreamEntry> stream) {
        streamStore.putIfAbsent(key, stream);
    }

    public List<StreamEntry> getStream(String key) {
        return streamStore.get(key);
    }

    public boolean containsStream(String key) {
        return streamStore.containsKey(key);
    }

    public String addStreamEntry(String key, String id, StreamEntry entry) {
        streamStore.putIfAbsent(key, new ArrayList<>());
        List<StreamEntry> stream = streamStore.get(key);

        if (id.equals("*")) {
            // generate full id: timestamp and sequence
            id = generateStreamId(stream);

        } else if (id.contains("-*")) {
            // generate partial id: timestamp provided, sequence auto-generated
            String[] parts = id.split("-");
            if (parts.length != 2 || !parts[1].equals("*")) {
                return null;
            }

            try {
                long millis = Long.parseLong(parts[0]);
                int seq = generateSequenceNumber(stream, millis);
                if (seq == -1) {
                    // special case: if millis is 0 and we need seq 0, it's invalid
                    if (millis == 0) return "ERR_ZERO_ZERO";
                    return null;
                }
                id = millis + "-" + seq;
            } catch (NumberFormatException e) {
                return null;
            }

        } else {
            // explicit id provided
            // check for 0-0 which is invalid special error
            String[] parts = id.split("-");
            if (parts.length == 2) {
                try {
                    long millis = Long.parseLong(parts[0]);
                    int seq = Integer.parseInt(parts[1]);
                    if (millis == 0 && seq == 0) return "ERR_ZERO_ZERO";
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            // check for id format and monotonicity validation
            if (!isValidStreamId(id, stream)) {
                return null;
            }
        }

        stream.add(new StreamEntry(id, entry.getFields()));
        return id;
    }

    private String generateStreamId(List<StreamEntry> stream) {
        long millis = System.currentTimeMillis();
        int seq = 0;

        if (!stream.isEmpty()) {
            String lastId = stream.get(stream.size() - 1).getId();
            String[] parts = lastId.split("-");
            long lastMillis = Long.parseLong(parts[0]);
            int lastSeq = Integer.parseInt(parts[1]);
            
            if (millis == lastMillis) {
                seq = lastSeq + 1;
            } else if (millis < lastMillis) {
                millis = lastMillis;
                seq = lastSeq + 1;
            }
        }

        return millis + "-" + seq;
    }

    private int generateSequenceNumber(List<StreamEntry> stream, long millis) {
        if (stream.isEmpty()) {
            // if first entry and millis is 0 then sequence must be 1
            return millis == 0 ? 1 : 0;
        }

        String lastId = stream.get(stream.size() - 1).getId();
        String[] parts = lastId.split("-");
        long lastMillis = Long.parseLong(parts[0]);
        int lastSeq = Integer.parseInt(parts[1]);

        if (millis > lastMillis) {
            return millis == 0 ? 1 : 0;
        } else if (millis == lastMillis) {
            return lastSeq + 1;
        } else {
            return -1;
        }
    }

    private boolean isValidStreamId(String id, List<StreamEntry> stream) {
        try {
            String[] parts = id.split("-");
            if (parts.length != 2) return false;
            
            long millis = Long.parseLong(parts[0]);
            int seq = Integer.parseInt(parts[1]);
            
            // Check monotonicity
            if (!stream.isEmpty()) {
                String lastId = stream.get(stream.size() - 1).getId();
                String[] lastParts = lastId.split("-");
                long lastMillis = Long.parseLong(lastParts[0]);
                int lastSeq = Integer.parseInt(lastParts[1]);
                
                if (millis < lastMillis) return false;
                if (millis == lastMillis && seq <= lastSeq) return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
