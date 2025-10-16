package com.example.jedis.storage;

public class ValueEntry {
    private Object value;
    private Long expiry;

    public ValueEntry() {}

    public ValueEntry(Object value, Long expiry) {
        this.value = value;
        this.expiry = expiry;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public Object getValue() {
        return value;
    }

    public Long getExpiry() {
        return expiry;
    }
}
