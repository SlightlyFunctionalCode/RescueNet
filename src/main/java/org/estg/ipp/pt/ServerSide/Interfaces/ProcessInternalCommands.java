package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.ServerSide.Services.GroupService;
import org.estg.ipp.pt.ServerSide.Services.UserService;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A interface {@code ProcessInternalCommands} define o contrato para o processamento de comandos internos no servidor.
 *
 * <p><b>Funcionalidades:</b></p>
 * <ul>
 *   <li>Gestão de confirmações de leitura de mensagens privadas.</li>
 *   <li>Registo e login de utilizadores.</li>
 *   <li>Gestão de logout de utilizadores.</li>
 *   <li>Gestão de pedidos de aprovação pendentes.</li>
 * </ul>
 *
 * <p>Esta interface é implementada por classes que lidam com a lógica dos comandos internos do sistema.</p>
 *
 * @see Permissions
 * @see GroupService
 * @see UserService
 */
public interface ProcessInternalCommands {

    /**
     * Recebe a confirmação de leitura de uma mensagem privada.
     *
     * @param payload Os dados associados à confirmação de leitura.
     * @throws IllegalArgumentException Se o payload for nulo ou inválido.
     */
    void handleIsReadConfirmation(String payload);

    /**
     * Processa o registo de um novo utilizador.
     *
     * @param payload Os dados associados ao registo, como nome de utilizador e password.
     * @param out O objeto {@link PrintWriter} usado para enviar as respostas ao cliente.
     * @throws IllegalArgumentException Se o payload for nulo ou inválido.
     */
    void handleRegister(String payload, PrintWriter out);

    /**
     * Processa o login de um utilizador.
     *
     * @param payload Os dados do login, como as credenciais do utilizador.
     * @param out O objeto {@link PrintWriter} usado para enviar as respostas ao cliente.
     * @param clientSocket O socket associado ao cliente que está a tentar fazer login.
     * @param usersWithPermissionsOnline Um mapa que contém os utilizadores online e as suas permissões associadas.
     * @throws IllegalArgumentException Se algum dos parâmetros obrigatórios for nulo ou inválido.
     */
    void handleLogin(String payload, PrintWriter out, Socket clientSocket, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline);

    /**
     * Processa o logout de um utilizador.
     *
     * @param username O nome ou identificador do utilizador que efetuou o logout.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     * @throws IllegalArgumentException Se o username for nulo ou inválido.
     */
    void handleLogout(String username, PrintWriter out);

    /**
     * Processa um pedido de aprovação pendente.
     *
     * @param payload Os dados associados ao pedido de aprovação pendente.
     * @throws IllegalArgumentException Se o payload for nulo ou inválido.
     */
    void handlePendingRequest(String payload);
}
