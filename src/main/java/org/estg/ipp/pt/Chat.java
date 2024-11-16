package org.estg.ipp.pt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class Chat {
    public static void startChat(String groupAddress, int port, String name) throws IOException {
        InetAddress group = InetAddress.getByName(groupAddress);

        MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(group);

        Thread receiveThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(received);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        receiveThread.start();

        Scanner scanner = new Scanner(System.in);
        System.out.println("JOIN ON CHAT " + groupAddress + ": ");

        while (true) {
            String msg = scanner.nextLine();
            if (msg.equalsIgnoreCase("exit")) {
                System.out.println("Saindo do chat...");
                socket.leaveGroup(group);
                socket.close();
                break;
            }
            String fullMsg = name + ": " + msg;
            byte[] buffer = fullMsg.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
        }
    }
}
