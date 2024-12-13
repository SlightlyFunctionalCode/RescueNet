package org.estg.ipp.pt.ClientSide.Interfaces;

import java.io.IOException;

/**
 * A interface {@code ChatService} define os métodos necessários para iniciar e parar um chat multicast.
 * Ela é implementada por classes responsáveis por gerir a comunicação em grupo num chat multicast.
 * Permite o envio e a receção de mensagens num grupo multicast.
 */
public interface ChatService {
    /**
     * Inicia uma sessão de chat, através da conexão do utilizador ao grupo especificado.
     *
     * <p>Este método deve ser implementado para permitir que o utilizador entre em um grupo de chat
     * usando o endereço do grupo, a porta e o nome fornecidos.</p>
     *
     * @param groupAddress o endereço IP do grupo multicast ao qual o utilizador deseja se conectar.
     * @param port a porta através da qual a comunicação multicast será realizada.
     * @param name o nome do utilizador, utilizado para identificação no chat.
     * @throws IOException se ocorrer um erro ao tentar conectar ao grupo ou iniciar a sessão de chat.
     */
    void startChat(String groupAddress, int port, String name) throws IOException;

    /**
     * Interrompe a sessão de chat, desconecta o utilizador do grupo e encerra qualquer comunicação ativa.
     *
     * <p>Este método deve ser implementado para garantir que o serviço de chat seja finalizado corretamente,
     * e deve limpar os recursos de forma a encerrar a comunicação com o grupo.</p>
     */
    void stopChat();
}
