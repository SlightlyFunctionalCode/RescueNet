package org.estg.ipp.pt.Services;

import jdk.swing.interop.SwingInterOpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
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

                    // Verifica se a mensagem é destinada ao usuário atual
                    if (received.startsWith("USER: " + name)) {
                        System.out.println("Mensagem direcionada a você: " + received.substring(("USER: " + name + " ").length()));
                    } else if (received.startsWith("NOTIFICAÇÃO:")) {
                        System.out.println("⚠️ " + received.substring(13)); // Exibe notificações
                    } else {
                        System.out.println(received);
                    }
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

            if (isCommand(msg)) {
                try (Socket serverSocket = new Socket("localhost", 5000);
                     PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {
                    out.println(msg + ":" + name); // Nome do usuário + comando
                    String serverResponse = in.readLine();

                    if (serverResponse.startsWith("PENDENTE")) {
                        System.out.println("Aguardando aprovação...");
                    } else if (serverResponse.startsWith("SUCESSO")) {
                        System.out.println("Comando aprovado e executado.");
                    } else if (serverResponse.startsWith("ERRO")) {
                        System.out.println("Erro: " + serverResponse);
                    }else if(serverResponse.startsWith("APPROVE")){
                        System.out.println("Aprovado e executado.");
                    }else if(serverResponse.startsWith("REJECT")){
                        System.out.println("Rejectado e executado.");
                    } else {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
                }
            } else {
                // Verifica se é uma mensagem dirigida a um usuário específico
                if (msg.startsWith("SEND TO:")) {
                    String targetUser = msg.split(":")[1].trim();
                    String fullMsg = "USER: " + targetUser + " " + name + ": " + msg.substring(targetUser.length() + 9); // Inclui o nome do usuário como destinatário
                    byte[] buffer = fullMsg.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(packet);
                } else {
                    // Caso contrário, envia para o grupo
                    String fullMsg = name + ": " + msg;
                    byte[] buffer = fullMsg.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(packet);
                }
            }
        }
    }

    private static boolean isCommand(String msg) {
        return msg.equalsIgnoreCase("MASS_EVACUATION") ||
                msg.equalsIgnoreCase("EMERGENCY_COMM") ||
                msg.equalsIgnoreCase("RESOURCE_DISTRIBUTION")||
                msg.startsWith("APPROVE") ||
                msg.startsWith("REJECT");
    }
}
