package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.TagType;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A interface {@code ProcessUserCommands} define o contrato para o processamento de comandos de utilizador no servidor.
 *
 * <p><b>Funcionalidades:</b></p>
 * <ul>
 *   <li>Exportação de logs com base em critérios específicos.</li>
 *   <li>Gestão das permissões de permissões dos utilizadores.</li>
 *   <li>Gestão dos grupos, como a criação, a entrada e a saída dos mesmos.</li>
 *   <li>Envio de mensagens privadas, alertas e gestão de aprovações.</li>
 *   <li>Visualização dos grupos e comandos disponíveis.</li>
 * </ul>
 *
 * <p>Esta interface é implementada por classes que gerem a lógica de comandos recebidos de utilizadores conectados ao sistema.</p>
 *
 * @see Permissions
 * @see TagType
 */
public interface ProcessUserCommands {

    /**
     * Processa um comando de exportação de logs do sistema. Pode ser filtrada por data e tags através do request
     *
     * @param request A solicitação de exportação de logs do sistema.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void processExport(String request, PrintWriter out);

    /**
     * Processa um comando de entrada num grupo.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param name O nome do grupo.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void processJoinCommand(String username, String name, PrintWriter out);

    /**
     * Processa um comando para alterar permissões de um utilizador.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param name O nome do utilizador que terá a permissão alterada.
     * @param permission A nova permissão atribuída.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out);

    /**
     * Processa um comando para criar um grupo.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param name O nome do grupo a ser criado.
     * @param publicOrPrivate Define se o grupo será público ou privado.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void processCreateGroupCommand(String username, String name, String publicOrPrivate, PrintWriter out);

    /**
     * Processa comandos de operação, ou seja ("/evac", "/resdist" e "/emerg") feitos por utilizadores.
     * Também envia notificações aos utilizadores com permissões acerca do pedido de operação.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param command O comando a ser processado.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     * @param usersWithPermissionsOnline O mapa de utilizadores online com as suas permissões associadas.
     */
    void processOperationCommand(String username, String command, PrintWriter out, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline);

    /**
     * Lida com comandos de aprovação.
     *
     * @param action A ação a ser realizada (ex.: APPROVE ou REJECT).
     * @param id O identificador do pedido a ser aprovado.
     * @param username O nome do utilizador que fez o pedido de aprovação ou rejeição.
     * @param requester O nome do utilizador que executou o comando de operação.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleApprovalCommand(String action, long id, String username, String requester, PrintWriter out);

    /**
     * Disponibiliza ao utilizador os comandos que este pode executar a partir das permissões do mesmo.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleCommandHelper(String username, PrintWriter out);

    /**
     * Lida com a adição de um utilizador a um grupo.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param userToAddName O nome do utilizador a adicionar.
     * @param group O nome do grupo ao qual o utilizador será adicionado.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleAddToGroup(String username, String userToAddName, String group, PrintWriter out);

    /**
     * Disponibiliza ao utilizador a lista de grupos a que este pode entrar e/ou faz parte.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleListGroups(String username, PrintWriter out);

    /**
     * Lida com o envio de uma mensagem de alerta para um utilizador.
     *
     * @param username O nome do utilizador destinatário.
     * @param message A mensagem de alerta a ser enviada.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleAlertMessage(String username, String message, PrintWriter out);

    /**
     * Lida com a saída de um utilizador de um grupo.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param group O nome do grupo que o utilizador deseja sair.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleLeaveGroup(String username, String group, PrintWriter out);

    /**
     * Lida com o logout de um utilizador do sistema. Atualiza o mapa de utilizadores com permissões online.
     *
     * @param username O nome do utilizador que fez o pedido.
     * @param usersWithPermissionsOnline O mapa de utilizadores online com as suas permissões associadas.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     */
    void handleLogout(String username, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline, PrintWriter out);
}
