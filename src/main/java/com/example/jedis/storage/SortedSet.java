package com.example.jedis.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public synchronized Integer getRank(String member) {
        if (!memberToScore.containsKey(member)) {
            return null;
        }

        Double targetScore = memberToScore.get(member);
        int rank = 0;

        for (Map.Entry<Double, Set<String>> entry : scoreToMember.entrySet()) {
            if (entry.getKey() < targetScore) {
                rank += entry.getValue().size();
            } else if (entry.getKey().equals(targetScore)) {
                List<String> sortedMembers = new ArrayList<>(entry.getValue());
                Collections.sort(sortedMembers);
                for (String m : sortedMembers) {
                    if (m.equals(member)) {
                        return rank;
                    }
                    rank++;
                }
            } else {
                break;
            }
        }

        return rank;
    }

    public synchronized List<String> getRangeByIndex(int start, int stop) {
        List<String> allMembers = getSortedMembers();
        int size = allMembers.size();

        if (size == 0) return new ArrayList<>();

        // handle negative indices
        if (start < 0) start += size;
        if (stop < 0) stop += size;

        // bounds checking
        if (start < 0) start = 0;
        if (stop >= size) stop = size - 1;

        if (start > stop) return new ArrayList<>();

        return new ArrayList<>(allMembers.subList(start, stop + 1));
    }

    public Double getScore(String member) {
        return memberToScore.get(member);
    }

    public int size() {
        return memberToScore.size();
    }

    public synchronized boolean remove(String member) {
        Double score = memberToScore.remove(member);
        if (score == null) {
            return false;
        }

        Set<String> bucket = scoreToMember.get(score);
        if (bucket != null) {
            bucket.remove(member);
            if (bucket.isEmpty()) {
                scoreToMember.remove(score);
            }
        }
        return true;
    }

    private synchronized List<String> getSortedMembers() {
        List<String> result = new ArrayList<>();
        for (Set<String> members : scoreToMember.values()) {
            List<String> sortedMembers = new ArrayList<>(members);
            Collections.sort(sortedMembers);
            result.addAll(sortedMembers);
        }
        return result;
    }
}
