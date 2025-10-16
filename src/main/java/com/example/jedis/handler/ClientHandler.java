package com.example.jedis.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.example.jedis.command.Command;
import com.example.jedis.command.CommandRegistry;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final CommandRegistry commandRegistry;

    public ClientHandler(Socket clientSocket, CommandRegistry commandRegistry) {
        this.clientSocket = clientSocket;
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void run() {
        try (clientSocket;
            OutputStream out = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()))) {
            
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.startsWith("*")) continue;

                String[] args = parseLine(line, in);
                if (args == null || args.length == 0 || args[0] == null) {
                    continue;
                }

                String commandName = args[0].toUpperCase();
                Command command = commandRegistry.getCommand(commandName);
                if (command != null) {
                    command.execute(args, out);
                } else {
                    out.write("-ERR unkown command\r\n".getBytes());
                    out.flush();
                    System.out.println("Unkown command: " + commandName);
                }
            }
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
    
    private String[] parseLine(String line, BufferedReader in) throws IOException {
        int arrayCount = Integer.parseInt(line.substring(1));
        String[] args = new String[arrayCount];

        for (int i = 0; i < args.length; i++) {
            String subLine = in.readLine();
            if (subLine == null || !subLine.startsWith("$")) break;

            String data = in.readLine();
            args[i] = data;
        }
        return args;
    }
}
