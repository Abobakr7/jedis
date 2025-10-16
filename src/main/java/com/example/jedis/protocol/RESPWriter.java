package com.example.jedis.protocol;

import java.io.IOException;
import java.io.OutputStream;

public class RESPWriter {
    private RESPWriter() {}

    public static void writeBulkString(OutputStream out, String msg) throws IOException {
        out.write(String.format("$%d\r\n%s\r\n", msg.length(), msg).getBytes());
        out.flush();
    }

    public static void writeNullBulkString(OutputStream out) throws IOException {
        out.write("$-1\r\n".getBytes());
        out.flush();
    }

    public static void writeInteger(OutputStream out, long num) throws IOException {
        out.write(String.format(":%d\r\n", num).getBytes());
        out.flush();
    }

    public static void writeSimpleString(OutputStream out, String msg) throws IOException {
        out.write(String.format("+%s\r\n", msg).getBytes());
        out.flush();
    }

    public static void writeError(OutputStream out, String msg) throws IOException {
        out.write(String.format("-ERR %s\r\n", msg).getBytes());
        out.flush();
    }

    public static void writeEmptyArray(OutputStream out) throws IOException {
        out.write("*0\r\n".getBytes());
        out.flush();
    }
}
