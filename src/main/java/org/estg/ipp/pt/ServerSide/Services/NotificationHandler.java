package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Server;
import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;


import static org.estg.ipp.pt.Server.getUserSocket;

/**
 * Classe responsável por gerir o envio de notificações e mensagens para utilizadores e grupos.
 *
 * <p>Esta classe fornece métodos para enviar notificações para utilizadores específicos,
 * mensagens diretas para utilizadores e mensagens multicast para grupos.</p>
 *
 * <p><b>Funcionalidades principais:</b></p>
 * <ol>
 *   <li>Envio de notificações para um utilizador, verificando se o socket está disponível.</li>
 *   <li>Envio de mensagens diretas para um utilizador, verificando a disponibilidade do utilizador.</li>
 *   <li>Envio de mensagens para grupos usando multicast, gerir a criação e comunicação com o gestor de multicast.</li>
 * </ol>
 *
 * <p><b>Comportamento:</b></p>
 * <ul>
 *   <li>Se o socket de um utilizador não estiver disponível ou estiver fechado, será exibida uma mensagem de erro.</li>
 *   <li>Se um utilizador estiver offline, a mensagem será guardada para ser enviada quando o utilizador se conectar.</li>
 *   <li>O envio de mensagens para grupos envolve a criação de um gestor de multicast se necessário.</li>
 * </ul>
 *
 * <p>Em caso de falha ao enviar a mensagem ou notificação, exceções de I/O serão tratadas com mensagens descritivas no console.</p>
 */
public class NotificationHandler {

    /**
     * Envia uma notificação para um utilizador específico.
     *
     * <p>Este método verifica se o socket do utilizador está disponível e, caso não esteja ou
     * esteja fechado, mostra uma mensagem de erro. Caso contrário, envia a mensagem para o utilizador
     * através do socket.</p>
     *
     * @param username O nome do utilizador para quem a notificação será enviada.
     * @param message A mensagem a ser enviada ao utilizador.
     */
    public static void notify(String username, String message) {
        Socket socket = getUserSocket(username);

        if (socket == null || socket.isClosed()) {
            System.out.println("Socket indisponível para " + username );
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            System.out.println("Notificação enviada para o utilizador " + username + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar notificação para " + username + ": " + e.getMessage());
        }
    }

    /**
     * Envia uma mensagem direta para um utilizador específico.
     *
     * <p>Este método tenta enviar uma mensagem para o socket do utilizador destino. Se o utilizador
     * estiver offline, a mensagem será guardada para ser enviada quando o utilizador fizer login.</p>
     *
     * @param targetUsername O nome do utilizador para quem a mensagem será enviada.
     * @param message A mensagem a ser enviada ao utilizador.
     */
    public static void sendMessage(String targetUsername, Message message) {
        Socket targetSocket = Server.getUserSocket(targetUsername);

        if (targetSocket != null) {
            try {
                PrintWriter targetOutput = new PrintWriter(targetSocket.getOutputStream(), true);
                targetOutput.println(message.getContent());
                System.out.printf("Envio de %s para o utilizador %s%n", message.getContent(), targetUsername);
            } catch (IOException e) {
                System.out.printf("Error sending message to %s%n", targetUsername);
            }
        } else {
            System.out.printf("Utilizador %s offline. A guardar mensagem para quando o utilizador fizer login%n", targetUsername);
        }
    }

    /**
     * Envia uma mensagem para um grupo usando multicast.
     *
     * <p>Este método utiliza o serviço de gestão de multicast para enviar a mensagem para todos os
     * membros de um grupo. Se o grupo não existir, será criado e configurado antes de enviar a mensagem.</p>
     *
     * @param notifyGroup O grupo para o qual a mensagem será enviada.
     * @param message A mensagem a ser enviada ao grupo.
     */
    public static void notifyGroup(Group notifyGroup, String message) {
        try {
            MulticastManagerService service = MulticastManagerService.getInstance();

            MulticastManager manager = service.getOrCreateMulticastManager(
                    notifyGroup.getAddress(),
                    notifyGroup.getPort()
            );

            manager.sendMessage(message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }
}
