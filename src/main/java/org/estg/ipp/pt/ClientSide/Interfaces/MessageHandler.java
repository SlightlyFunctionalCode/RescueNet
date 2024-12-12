package org.estg.ipp.pt.ClientSide.Interfaces;

import java.io.IOException;

/**
 * A interface {@code MessageHandler} define o contrato para classes responsáveis por
 * enviar e receber mensagens num sistema de chat.
 *
 * <p>Implementações dessa interface devem ser capazes de enviar mensagens para um destino
 * específico e iniciar o processo de receção de mensagens de outros participantes no chat.</p>
 */
public interface MessageHandler {
    /**
     * Envia uma mensagem para o destino especificado.
     *
     * <p>Este método é responsável por enviar uma mensagem para um grupo de chat ou
     * outro destino, dependendo da implementação específica. A mensagem é fornecida
     * como uma string.</p>
     *
     * @param message a mensagem a ser enviada.
     * @throws IOException se ocorrer um erro ao enviar a mensagem.
     */
    void sendMessage(String message) throws IOException;

    /**
     * Inicia o recebimento de mensagens de outros participantes.
     *
     * <p>Este método permite que o sistema comece a receber mensagens enviadas por
     * outros utilizadores no chat. As mensagens recebidas são passadas para o receptor
     * especificado.</p>
     *
     * @param receiver o objeto que irá receber as mensagens.
     *                 O recetor deve implementar a interface {@code MessageReceiver}
     *                 para processar as mensagens recebidas.
     */
    void startReceivingMessages(MessageReceiver receiver);
}
