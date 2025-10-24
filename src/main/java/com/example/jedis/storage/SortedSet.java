package com.example.jedis.storage;

import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class SortedSet {
    private final ConcurrentHashMap<String, Double> memberToScore = new ConcurrentHashMap<>();
    private final TreeMap<Double, Set<String>> scoreToMember = new TreeMap<>();

    public synchronized boolean add(String member, double score) {
        Double oldScore = memberToScore.get(member);
        if (oldScore != null) {
            if (oldScore.equals(score)) {
                return false; // same score, no change
            }

            Set<String> oldBucket = scoreToMember.get(oldScore);
            if (oldBucket != null) {
                oldBucket.remove(member);
                if (oldBucket.isEmpty()) {
                    scoreToMember.remove(oldScore);
                }
            }
        }

        memberToScore.put(member, score);
        Set<String> bucket = scoreToMember.get(score);
        if (bucket == null) {
            bucket = ConcurrentHashMap.newKeySet();
            scoreToMember.put(score, bucket);
        }
        bucket.add(member);

        return oldScore == null;
    }

    
}
