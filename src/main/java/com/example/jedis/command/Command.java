package com.example.jedis.command;

import java.io.OutputStream;

public interface Command {
    void execute(String[] params, OutputStream out) throws Exception;
}
