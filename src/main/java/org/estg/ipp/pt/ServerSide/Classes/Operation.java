package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.Permissions;

/**
 * Classe que representa uma operação com um nome e uma permissão necessária para sua execução.
 *
 * Esta classe é utilizada para armazenar informações sobre uma operação específica que pode ser realizada num sistema,
 * incluindo o nome da operação e a permissão necessária para que ela seja executada.
 */
public class Operation {
    private final String name;
    private final Permissions requiredPermission;

    /**
     * Construtor da classe Operation.
     *
     * @param name O nome da operação.
     * @param requiredPermission A permissão necessária para executar a operação.
     */
    public Operation(String name, Permissions requiredPermission) {
        this.name = name;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Obtém o nome da operação.
     *
     * @return O nome da operação.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém a permissão necessária para a operação.
     *
     * @return A permissão necessária para executar a operação.
     */
    public Permissions getRequiredPermission() {
        return requiredPermission;
    }
}
