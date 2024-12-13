package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Server;
import org.springframework.stereotype.Component;

/**
 * Classe que mantém e fornece as estatísticas relacionadas ao servidor, como o número total de comandos executados e o
 * número de utilizadores online.
 * Esta classe é thread-safe, e utiliza métodos sincronizados para garantir que as estatísticas sejam atualizadas e
 * acedidas de forma segura em um ambiente multi-threaded.
 */
@Component
public class ServerStats {
    private int totalCommandsExecuted = 0;

    /**
     * Aumenta o contador de comandos executados no servidor.
     * Este método é sincronizado para garantir a segurança em ambientes multi-threaded.
     */
    public synchronized void incrementCommandsExecuted() {
        totalCommandsExecuted++;
    }

    /**
     * Devolve o número total de comandos executados no servidor.
     * Este método é sincronizado para garantir a segurança em ambientes multi-threaded.
     *
     * @return O número total de comandos executados.
     */
    public synchronized int getTotalCommandsExecuted() {
        return totalCommandsExecuted;
    }

    /**
     * Devolve o número de utilizadores atualmente conectados ao servidor.
     * Este método é sincronizado para garantir a segurança em ambientes multi-threaded.
     *
     * @return O número de utilizadores conectados.
     */
    public synchronized int getConnectedUsers() {
        return Server.getNumberOfClients();
    }
}
