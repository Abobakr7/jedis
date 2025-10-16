package com.example.jedis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.jedis.command.CommandRegistry;
import com.example.jedis.handler.ClientHandler;

public class App {
    private static final int PORT = 6379;
    private static final CommandRegistry commandRegistry = new CommandRegistry();

    public static void main(String[] args) {
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
}
