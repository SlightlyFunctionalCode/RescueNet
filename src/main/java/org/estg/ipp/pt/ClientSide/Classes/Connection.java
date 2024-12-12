package org.estg.ipp.pt.ClientSide.Classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Connection(String serverAddress, int serverPort) throws IOException {
        connect(serverAddress, serverPort);
    }

    public void connect(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void reconnect() {
        try {
            socket.close();
            connect("localhost", 5000);
        } catch (IOException e) {
            System.out.println("Erro ao tentar reconectar: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public Socket getSocket() {
        return socket;
    }
}
