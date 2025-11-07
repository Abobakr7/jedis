package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;
import com.example.jedis.storage.GeoCode;
import com.example.jedis.storage.JedisStore;
import com.example.jedis.storage.SortedSet;

public class GeoPos implements Command {
    private final JedisStore store;

    public GeoPos(JedisStore store) {
        this.store = store;
    }

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length < 3) {
            RESPWriter.writeError(out, "wrong number of arguments for 'geopos'");
            return;
        }

        String key = args[1];
        SortedSet zset = store.getSortedSet(key);

        out.write(String.format("*%d\r\n", args.length - 2).getBytes());
        out.flush();

        for (int i = 2; i < args.length; i++) {
            String member = args[i];

            if (zset == null || !zset.contains(member)) {
                out.write("*-1\r\n".getBytes());
                out.flush();
            } else {
                double geoCode = zset.getScore(member);

                double[] coords = GeoCode.decode(geoCode);
                double latitude = coords[0], longitude = coords[1];

                out.write(("*2\r\n").getBytes());
                RESPWriter.writeBulkString(out, formatCoordinate(longitude));
                RESPWriter.writeBulkString(out, formatCoordinate(latitude));
            }
        }
    }

    private String formatCoordinate(double coord) {
        // Redis returns coordinates with ~11 decimal places
        return String.format("%.11f", coord);
    }
}
