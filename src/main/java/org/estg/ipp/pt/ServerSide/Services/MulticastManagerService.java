package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço para gerir os gestores de multicast no sistema.
 *
 * <p>O serviço oferece as funcionalidades para criar e gerir as instâncias de gestores de multicast
 * para diferentes grupos e portas. Ele permite a criação, a recuperação e a remoção de gestores de multicast,
 * além de garantir que apenas uma instância de cada gestor de multicast seja criada para um dado grupo e porta.</p>
 *
 * <p><b>Funcionalidades principais:</b></p>
 * <ol>
 *   <li>Criação de novos gestores de multicast, caso não existam para o grupo e porta especificados.</li>
 *   <li>Recuperação de gestores de multicast existentes.</li>
 * </ol>
 *
 * <p>Em caso de falha em operações, como falhas ao tentar receber ou fechar conexões, exceções de I/O serão lançadas.</p>
 */
public class MulticastManagerService {
    private static MulticastManagerService instance;
    private final Map<String, MulticastManager> multicastManagers = new HashMap<>();

    /**
     * Construtor privado para impedir instâncias adicionais.
     */
    private MulticastManagerService() {}

    /**
     * Obtém a única instância do serviço MulticastManagerService (singleton).
     *
     * @return A instância única do MulticastManagerService.
     */
    public static synchronized MulticastManagerService getInstance() {
        if (instance == null) {
            instance = new MulticastManagerService();
        }
        return instance;
    }

    /**
     * Obtém um MulticastManager existente ou cria um para o endereço e porta especificados.
     *
     * @param groupAddress O endereço do grupo multicast.
     * @param port A porta do grupo multicast.
     * @return O MulticastManager associado ao grupo e porta fornecidos.
     * @throws IOException Se ocorrer um erro ao tentar receber mensagens do grupo.
     */
    public MulticastManager getOrCreateMulticastManager(String groupAddress, int port) throws IOException {
        String groupKey = groupAddress + ":" + port;
        if (!multicastManagers.containsKey(groupKey)) {
            MulticastManager manager = new MulticastManager(groupAddress, port);
            multicastManagers.put(groupKey, manager);
            manager.receiveMessages();
        }
        return multicastManagers.get(groupKey);
    }

    /**
     * Remove um MulticastManager para o endereço e porta especificados, se existir.
     *
     * @param groupAddress O endereço do grupo multicast.
     * @param port A porta do grupo multicast.
     * @throws IOException Se ocorrer um erro ao tentar fechar a conexão do MulticastManager.
     */
    public void removeMulticastManager(String groupAddress, int port) throws IOException {
        String groupKey = groupAddress + ":" + port;
        MulticastManager manager = multicastManagers.remove(groupKey);
        if (manager != null) {
            manager.close();
        }
    }

    /**
     * Obtém um MulticastManager existente para o endereço e porta especificados.
     *
     * @param groupAddress O endereço do grupo multicast.
     * @param port A porta do grupo multicast.
     * @return O MulticastManager associado ao grupo e porta fornecidos, ou null se não existir.
     */
    public MulticastManager getMulticastManager(String groupAddress, int port) {
        return multicastManagers.get(groupAddress + ":" + port);
    }
}