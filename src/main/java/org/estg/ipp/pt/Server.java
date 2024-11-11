package org.estg.ipp.pt;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Server {
    public static void main(String[] args) throws IOException {
        // Endereço do grupo multicast e porta
        InetAddress group = InetAddress.getByName("230.0.0.0");
        int sendPort = 4446;
        int receivePort = 4447; // Porta diferente para receber respostas
        int numClients = 2; // Número conhecido de clientes
        int timeoutMs = 10000; // Tempo máximo de recepção (10 segundos)

        // Socket para enviar datagramas
        DatagramSocket sendSocket = new DatagramSocket();

        // Socket para receber datagramas
        DatagramSocket receiveSocket = new DatagramSocket(receivePort);
        receiveSocket.setSoTimeout(timeoutMs); // Definir tempo máximo de recepção

        System.out.println("Servidor iniciado.");

        int cycles = 1;
        int iterationNum = 0;
        while (iterationNum < cycles) {
            String message = "**What's the local time on your clock?**: ";
            byte[] buf = message.getBytes();

            long startTime = System.nanoTime();
            // Enviar o pacote para o grupo multicast
            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, group, sendPort);
            sendSocket.send(sendPacket);

            // Aguardar respostas dos clientes
            int responsesReceived = 0;
            long sumOfSeconds = 0;
            long sumOfNanos = 0;
            while (responsesReceived < numClients) {
                try {
                    boolean responseReceived = false;
                    byte[] receiveBuf = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                    receiveSocket.receive(receivePacket);
                    if (receivePacket.getData() != null) {
                        responseReceived = true;
                    }
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    long estimatedTime = System.nanoTime() - startTime;
                    LocalDateTime adjustedTime = LocalDateTime.parse(response).minusNanos(estimatedTime / 2);
                    // Converta o tempo ajustado para nanossegundos desde a época
                    sumOfSeconds += adjustedTime.toEpochSecond(ZoneOffset.UTC);
                    sumOfNanos += adjustedTime.getNano(); // Acumule os nanossegundos
                    System.out.println("Resposta de " + receivePacket.getAddress() + ": " + response);
                    System.out.println("Tempo ajustado: " + adjustedTime);
                    System.out.println("  RTT: " + (responseReceived ? estimatedTime / 1e6 + " ms" : "N/A"));
                    System.out.println("  Perda de pacote: " + (responseReceived ? "NÃO" : "SIM"));
                    responsesReceived++;
                } catch (SocketTimeoutException e) {
                    System.out.println("Tempo limite de recepcao alcancado. Respostas recebidas: " + responsesReceived + "/" + numClients);
                    break;
                }
            }

            System.out.println("Respostas totais: " + responsesReceived + "/" + numClients);
            System.out.println("Porcentagem de respostas: " + (responsesReceived / (float) numClients) * 100 + "%");
            if (responsesReceived != 0) {
                long averageSeconds = sumOfSeconds / responsesReceived; // Média em nanossegundos
                int averageNanos = (int) sumOfNanos / responsesReceived;
                // Manter a precisão usando Instant
                LocalDateTime averageTime = LocalDateTime.ofEpochSecond(averageSeconds, averageNanos, ZoneOffset.UTC);

                System.out.println("Média dos tempos ajustados: " + averageTime);


                byte[] adjustTimeBuf = averageTime.toString().getBytes();

                // Enviar o pacote para o grupo multicast
                DatagramPacket adjustTimePacket = new DatagramPacket(adjustTimeBuf, adjustTimeBuf.length, group, sendPort);
                sendSocket.send(adjustTimePacket);

                iterationNum++;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
