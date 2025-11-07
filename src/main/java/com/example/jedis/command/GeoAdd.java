package com.example.jedis.command;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.GeoCode;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class GeoAdd implements Command {
    private final JedisStore store;

    public GeoAdd(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 5) {
            RESPWriter.writeError(out, "wrong number of arguments for 'geoadd");
            return;
        }

        // check that args are triplets (longitude, latitude, member)
        if ((args.length - 2) % 3 != 0) {
            RESPWriter.writeError(out, "wrong number of arguments for 'geoadd");
            return;
        }
        
        List<GeoStore> geoList = new ArrayList<>();
        for (int i = 2; i < args.length; i += 3) {
            try {
                double longitude = Double.parseDouble(args[i]);
                double latitude = Double.parseDouble(args[i + 1]);
                String member = args[i + 2];

                if (GeoCode.isValidCoordinates(longitude, latitude)) {
                    throw new IllegalArgumentException(String.format("invalid longitude,latitude pair %d,%d", longitude, latitude));
                }

                geoList.add(new GeoStore(longitude, latitude, member));

            } catch (NumberFormatException e) {
                RESPWriter.writeError(out, "value is not a valid float");
                geoList.clear();
                return;
            } catch (IllegalArgumentException e) {
                RESPWriter.writeError(out, e.getMessage());
                geoList.clear();
                return;
            }
        }

        String key = args[1];
        SortedSet zset = store.getOrCreateSortedSet(key);
        
        for (GeoStore g : geoList) {
            long geoCode = GeoCode.encode(g.getLat(), g.getLong());
            zset.add(g.getMember(), geoCode);
        }

        RESPWriter.writeInteger(out, geoList.size());
    }

    private class GeoStore {
        private double longitude;
        private double latitude;
        private String member;

        public GeoStore(double longitude, double latitude, String member) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.member = member;
        }

        public double getLong() {
            return longitude;
        }

        public double getLat() {
            return latitude;
        }

        public String getMember() {
            return member;
        }
    }
}
