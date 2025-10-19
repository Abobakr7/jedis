package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;

public class Echo implements Command {

    @Override
    public void execute(String[] args, OutputStream out) throws Exception {
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) sb.append(" ");
            }
            String res = sb.toString();
            RESPWriter.writeBulkString(out, res);
            out.write(String.format("$%d\r\n%s\r\n", res.length(), res).getBytes());
        } else {
            RESPWriter.writeError(out, "wrong number of arguments for 'echo'");
        }
    }
}
