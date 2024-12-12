package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.ServerSide.Interfaces.ExecuteInternalCommands;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessInternalCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A classe {@code ExecuteInternalCommandsImpl} implementa a interface {@link ExecuteInternalCommands}
 * e é responsável por processar e executar comandos internos recebidos pelo servidor.
 *
 * <p>Esta classe verifica se um comando é um comando interno válido e, em seguida, encaminha o comando para
 * o respetivo processamento por meio da classe {@link ProcessInternalCommands}.</p>
 */
@Component
public class ExecuteInternalCommandsImpl implements ExecuteInternalCommands {

    @Autowired
    ProcessInternalCommands processInternalCommands;

    /**
     * Verifica se o comando fornecido é um comando interno válido.
     *
     * <p>Comandos válidos incluem "REGISTER", "LOGIN", "LOGOUT", "READY" e "CONFIRM_READ".</p>
     *
     * @param command o comando a ser verificado.
     * @return {@code true} se o comando for um comando interno válido, {@code false} caso contrário.
     */
    public boolean isInternalCommand(String command) {
        return command.equals("REGISTER") || command.equals("LOGIN") || command.equals("LOGOUT") || command.equals("READY") || command.equals("CONFIRM_READ");
    }

    /**
     * Processa o comando interno recebido e chama o método apropriado da classe {@link ProcessInternalCommands}.
     *
     * <p>O comando é analisado e, dependendo do tipo, a ação correspondente é realizada. Se o comando for inválido,
     * uma mensagem de erro é enviada ao cliente.</p>
     *
     * @param command o comando a ser processado.
     * @param payload os dados adicionais associados ao comando.
     * @param out o {@link PrintWriter} usado para enviar respostas ao cliente.
     * @param clientSocket o {@link Socket} do cliente.
     * @param groupList a lista de grupos associados ao servidor.
     * @param usersWithPermissionsOnline o mapa contendo os utilizadores online e as suas permissões.
     */
    public void handleInternalCommand(String command, String payload, PrintWriter out, Socket clientSocket, List<Group> groupList, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) {
        switch (command) {
            case "REGISTER" -> processInternalCommands.handleRegister(payload, out);
            case "LOGIN" -> processInternalCommands.handleLogin(payload, out, clientSocket, usersWithPermissionsOnline);
            case "LOGOUT" -> processInternalCommands.handleLogout(payload, out);
            case "READY" -> processInternalCommands.handlePendingRequest(payload);
            case "CONFIRM_READ" -> new Thread(() -> processInternalCommands.handleIsReadConfirmation(command + ":" + payload)).start();

            default -> out.println("ERRO: Comando interno inválido");
        }
    }
}


