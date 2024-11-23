package org.estg.ipp.pt.Services;

import java.io.*;
import java.net.*;

class ChatHandler implements Runnable {
    private final Socket clientSocket;
    private final Socket otherClientSocket;

    public ChatHandler(Socket clientSocket, Socket otherClientSocket) {
        this.clientSocket = clientSocket;
        this.otherClientSocket = otherClientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(otherClientSocket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                // Forward message to the other client
                out.println(message);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                otherClientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing sockets: " + e.getMessage());
            }
        }
    }
}

