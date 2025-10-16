package com.example.jedis.command;

import java.io.OutputStream;

public class PingCommand implements Command {

    @Override
    public void execute(String[] params, OutputStream out) throws Exception {
        out.write("+PONG\r\n".getBytes());
        out.flush();
    }
    
}
