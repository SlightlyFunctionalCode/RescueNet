package org.estg.ipp.pt.ClientSide.Interfaces;

/**
 * A interface {@code MessageReceiver} define o contrato para as classes responsáveis por
 * processar as mensagens recebidas num sistema de chat.
 *
 * <p>As implementações desta interface devem ser capazes de lidar com as mensagens
 * recebidas, por exemplo, mostrá-las no chat ou realizar outras ações necessárias
 * com o conteúdo da mensagem.</p>
 */
public interface MessageReceiver {
    /**
     * Processa uma mensagem recebida.
     *
     * <p>Este método é chamado quando uma nova mensagem é recebida de outro participante
     * no chat. A mensagem recebida é fornecida como uma string, e a implementação
     * específica pode determinar como essa mensagem será processada (como mostrar no ecrã
     * ou realizar outra ação).</p>
     *
     * @param message a mensagem recebida.
     */
    void onMessageReceived(String message);
}
