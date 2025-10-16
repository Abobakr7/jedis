package com.example.jedis.command;

import java.io.OutputStream;

public interface Command {
    void execute(String[] args, OutputStream out) throws Exception;
}
