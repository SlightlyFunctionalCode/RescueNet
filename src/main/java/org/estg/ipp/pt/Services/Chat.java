package org.estg.ipp.pt.Services;

import jdk.swing.interop.SwingInterOpUtils;
import org.estg.ipp.pt.Classes.Enum.RegexPatterns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                    if (RegexPatterns.DIRECTED_MESSAGE.matcher(received).matches()) {
                        System.out.println("Mensagem direcionada a você: " +
                                RegexPatterns.DIRECTED_MESSAGE.matcher(received).replaceFirst("$2"));
                    } else if (RegexPatterns.NOTIFICATION.matcher(received).matches()) {
                        System.out.println("⚠️ " +
                                RegexPatterns.NOTIFICATION.matcher(received).replaceFirst("$1").trim());
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

            if (msg.startsWith("/")) {
                try (Socket serverSocket = new Socket("localhost", 5000);
                     PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {
                    out.println(msg + ":" + name); // Nome do user + comando

                    String serverResponse = in.readLine();

                    // Use regex enums to process server responses
                    if (RegexPatterns.SERVER_PENDING.matches(serverResponse)) {
                        System.out.println("Aguardando aprovação...");
                    } else if (RegexPatterns.SERVER_SUCCESS.matches(serverResponse)) {
                        System.out.println("Comando aprovado e executado.");
                    } else if (RegexPatterns.SERVER_ERROR.matcher(serverResponse).matches()) {
                        System.out.println("Erro: " +
                                RegexPatterns.SERVER_ERROR.matcher(serverResponse).replaceFirst("$1"));
                    } else if (RegexPatterns.SERVER_APPROVE.matches(serverResponse)) {
                        System.out.println("Aprovado e executado.");
                    } else if (RegexPatterns.SERVER_REJECT.matches(serverResponse)) {
                        System.out.println("Rejeitado.");
                    } else if (RegexPatterns.SERVER_REJECT.matches(serverResponse)) {
                        System.out.println("Rejeitado.");
                    } else if (serverResponse.startsWith("--HELP--")) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.equals("--END HELP--")) break;
                            System.out.println(line);
                        }
                    }else {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
                }
            } else {
                Pattern sendToPattern = Pattern.compile("^SEND TO:(.*)");

                Matcher sendToMatcher = sendToPattern.matcher(msg);
                if (sendToMatcher.find()) {
                    String targetUser = RegexPatterns.SEND_TO.matcher(msg).replaceFirst("$1").trim();
                    String fullMsg = "USER: " + targetUser + " " + name + ": " +
                            msg.substring(("SEND TO:" + targetUser).length()).trim();
                    byte[] buffer = fullMsg.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(packet);
                } else {
                    // Otherwise, send it to the group
                    String fullMsg = name + ": " + msg;
                    byte[] buffer = fullMsg.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(packet);
                }
            }
        }
    }

}
