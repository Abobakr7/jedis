package com.example.jedis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.jedis.command.CommandRegistry;
import com.example.jedis.handler.ClientHandler;

public class App {
    private static int PORT = 6379;
    private static final CommandRegistry commandRegistry = new CommandRegistry();

    public static void main(String[] args) {
        parseArguments(args);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // SO_REUSEADDR ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            System.out.println(String.format("Server started successfully on port %d", PORT));
            System.out.println("Waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(new ClientHandler(clientSocket, commandRegistry)).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                    if (i + 1 >= args.length) {
                        exitWithError("--port requires port number");
                    }
                    try {
                        PORT = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        exitWithError("Invalid port number '" + args[i + 1] + "'");
                    }
                    break;
            
                default:
                    break;
            }
        }
    }

    private static void exitWithError(String message) {
        System.err.println("Error: " + message);
        System.exit(1);
    }
}
