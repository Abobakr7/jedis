package com.example.jedis.command;

import java.io.OutputStream;

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
            out.write(String.format("$%d\r\n%s\r\n", res.length(), res).getBytes());
        } else {
            out.write("-ERR wrong number of arguments for 'ECHO'\r\n".getBytes());
        }
        out.flush();
    }
}
