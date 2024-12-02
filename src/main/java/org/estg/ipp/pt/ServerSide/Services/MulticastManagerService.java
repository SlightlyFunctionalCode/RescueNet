package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.ServerSide.Classes.Messagehandler;
import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class MulticastManagerService {
    private static MulticastManagerService instance;
    private final Map<String, MulticastManager> multicastManagers = new HashMap<>();

    // Construtor privado para evitar instância direta
    private MulticastManagerService() {}

    // Método para obter a única instância
    public static synchronized MulticastManagerService getInstance() {
        if (instance == null) {
            instance = new MulticastManagerService();
        }
        return instance;
    }

    public MulticastManager getOrCreateMulticastManager(String groupAddress, int port) throws IOException {
        String groupKey = groupAddress + ":" + port;
        if (!multicastManagers.containsKey(groupKey)) {
            MulticastManager manager = new MulticastManager(groupAddress, port);
            multicastManagers.put(groupKey, manager);
            manager.receiveMessages();
        }
        return multicastManagers.get(groupKey);
    }

    public void removeMulticastManager(String groupAddress, int port) throws IOException {
        String groupKey = groupAddress + ":" + port;
        MulticastManager manager = multicastManagers.remove(groupKey);
        if (manager != null) {
            manager.close();
        }
    }

    public MulticastManager getMulticastManager(String groupAddress, int port) {
        return multicastManagers.get(groupAddress + ":" + port);
    }
}