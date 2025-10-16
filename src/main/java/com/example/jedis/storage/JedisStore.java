package com.example.jedis.storage;

import java.util.concurrent.ConcurrentHashMap;

public class JedisStore {
    private final ConcurrentHashMap<String, ValueEntry> store = new ConcurrentHashMap<>();

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
}
