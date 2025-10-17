package com.example.jedis.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class JedisStore {
    private final ConcurrentHashMap<String, ValueEntry> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Object>> listStore = new ConcurrentHashMap<>();

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

    public ConcurrentLinkedDeque<Object> getList(String key) {
        return listStore.get(key);
    }

    public void putListIfAbsent(String key, ConcurrentLinkedDeque<Object> list) {
        listStore.putIfAbsent(key, list);
    }

    public boolean containsList(String key) {
        return listStore.containsKey(key);
    }
}
