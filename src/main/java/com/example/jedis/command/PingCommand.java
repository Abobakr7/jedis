package com.example.jedis.command;

import java.io.OutputStream;

import com.example.jedis.protocol.RESPWriter;

public class PingCommand implements Command {

    @Override
    public void execute(String[] params, OutputStream out) throws Exception {
        RESPWriter.writeSimpleString(out, "PONG");
    }
    
}
