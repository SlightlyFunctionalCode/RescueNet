package org.estg.ipp.pt.ClientSide.Interfaces;

/**
 * A interface {@code InputHandler} define o contrato para classes responsáveis por
 * processar a entrada do utilizador num sistema de chat.
 *
 * <p>Implementações dessa interface devem ser capazes de ler as entradas do utilizador,
 * interpretar comandos ou mensagens, e encaminhá-las para os manipuladores apropriados
 * (como {@code CommandHandler} ou {@code MessageHandler}).</p>
 */
public interface InputHandler {

    /**
     * Processa a entrada do utilizador, interpretando comandos ou mensagens.
     *
     * <p>Este método é responsável por capturar e processar as entradas fornecidas
     * pelo usuário no sistema de chat. Isso inclui a interpretação de comandos (como
     * "/logout" ou "/commands") e o envio de mensagens normais para o sistema de chat.</p>
     *
     * @param name o nome do utilizador que está a fornecer a entrada.
     *             Esse parâmetro pode ser utilizado para associar a entrada a um utilizador específico.
     */
    void handleInput(String name);
}
