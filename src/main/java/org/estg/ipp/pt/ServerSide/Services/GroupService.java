package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.ServerSide.Repositories.GroupRepository;
import org.estg.ipp.pt.ServerSide.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço para gerir os grupos no sistema.
 *
 * <p>O serviço oferece funcionalidades para inicializar os grupos base,
 * adicionar ou remover utilizadores de grupos, verificar a associação de utilizadores a grupos
 * e criar ou manipular os grupos personalizados.</p>
 *
 * <p><b>Funcionalidades principais:</b></p>
 * <ol>
 *   <li>Inicialização dos grupos base.</li>
 *   <li>Adição de utilizadores a grupos existentes.</li>
 *   <li>Verificação e recuperação de grupos com base em utilizadores e permissões.</li>
 *   <li>Criação de grupos personalizados.</li>
 *   <li>Remoção de utilizadores de grupos, com validações de permissões e regras de negócio.</li>
 * </ol>
 *
 * <p>Se as operações falharem devido a dados inválidos ou inconsistentes,
 * exceções serão lançadas com mensagens descritivas.</p>
 */
@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Inicializa os grupos base no sistema.
     *
     * <p>Os grupos base criados incluem:</p>
     * <ul>
     *   <li>GERAL: Grupo público sem restrição de permissões.</li>
     *   <li>LOW_LEVEL: Grupo público com permissões de baixo nível.</li>
     *   <li>MEDIUM_LEVEL: Grupo público com permissões de nível médio.</li>
     *   <li>HIGH_LEVEL: Grupo público com permissões de alto nível.</li>
     * </ul>
     *
     * <p>Se os grupos já existirem, nenhuma ação será realizada.</p>
     */
    public void initializeDefaultGroups() {
        if (groupRepository.count() == 0) {
            Group groupGeral = new Group();
            groupGeral.setName("GERAL");
            groupGeral.setAddress("230.0.0.0");
            groupGeral.setPort(4446);
            groupGeral.setPublic(true);
            groupGeral.setRequiredPermissions(Permissions.NO_LEVEL);

            Group groupLowLevel = new Group();
            groupLowLevel.setName("LOW_LEVEL");
            groupLowLevel.setAddress("230.0.0.1");
            groupLowLevel.setPort(4447);
            groupLowLevel.setPublic(true);
            groupLowLevel.setRequiredPermissions(Permissions.LOW_LEVEL);

            Group groupMediumLevel = new Group();
            groupMediumLevel.setName("MEDIUM_LEVEL");
            groupMediumLevel.setAddress("230.0.0.2");
            groupMediumLevel.setPort(4448);
            groupMediumLevel.setPublic(true);
            groupMediumLevel.setRequiredPermissions(Permissions.MEDIUM_LEVEL);

            Group groupHighLevel = new Group();
            groupHighLevel.setName("HIGH_LEVEL");
            groupHighLevel.setAddress("230.0.0.3");
            groupHighLevel.setPort(4449);
            groupHighLevel.setPublic(true);
            groupHighLevel.setRequiredPermissions(Permissions.HIGH_LEVEL);

            groupRepository.save(groupGeral);
            groupRepository.save(groupLowLevel);
            groupRepository.save(groupMediumLevel);
            groupRepository.save(groupHighLevel);

            System.out.println("Grupos base criados com sucesso!");
        } else {
            System.out.println("Grupos base já existem no sistema.");
        }
    }

    /**
     * Adiciona um utilizador a um grupo específico.
     *
     * @param groupName Nome do grupo ao qual o utilizador será adicionado.
     * @param user Utilizador que será adicionado ao grupo.
     * @throws IllegalArgumentException Se o grupo ou o utilizador não forem encontrados,
     *                                  ou se o utilizador já estiver no grupo.
     */
    @Transactional
    public void addUserToGroup(String groupName, User user) throws IllegalArgumentException {

        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado"));

        if (groupRepository.existsByIdAndUsersId(group.getId(), user.getId())) {
            throw new IllegalArgumentException("Utilizador já está no grupo");
        }

        group.getUsers().add(user);
        user.setCurrentGroup(group);

        groupRepository.save(group);
        System.out.println("Utilizador adicionado ao grupo com sucesso!");
    }

    /**
     * Obtém um grupo pelo nome e verifica a associação de um utilizador.
     *
     * @param userId ID do utilizador a ser verificado.
     * @param groupName Nome do grupo a ser buscado.
     * @return O grupo encontrado.
     * @throws IllegalArgumentException Se o grupo ou a associação do utilizador não forem encontrados.
     */
    @Transactional
    public Group getUserGroupByNameAndVerify(Long userId, String groupName) {
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));

        boolean userInGroup = group.getUsers().stream().anyMatch(user -> user.getId().equals(userId));

        if (!userInGroup) {
            throw new IllegalArgumentException("Utilizador com ID " + userId + " não pertence ao grupo " + groupName);
        }

        return group;
    }

    /**
     * Obtém todos os grupos registados no sistema.
     *
     * @return Lista de todos os grupos.
     */
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    /**
     * Verifica se um utilizador está associado a um grupo específico.
     *
     * @param groupName Nome do grupo.
     * @param userId ID do utilizador.
     * @return {@code true} se o utilizador pertence ao grupo, {@code false} caso contrário.
     * @throws IllegalArgumentException Se o grupo não for encontrado.
     */
    @Transactional
    public boolean isUserInGroup(String groupName, Long userId) {
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));

        return group.getUsers().stream().anyMatch(user -> user.getId().equals(userId));
    }

    /**
     * Obtém um grupo pelo nome.
     *
     * @param groupName Nome do grupo.
     * @return O grupo encontrado.
     * @throws IllegalArgumentException Se o grupo não for encontrado.
     */
    public Group getGroupByName(String groupName) throws IllegalArgumentException {

        return groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));
    }

    /**
     * Cria um grupo personalizado com configurações específicas.
     *
     * @param id ID do utilizador que criou o grupo.
     * @param name Nome do grupo.
     * @param publicOrPrivate Indica se o grupo será público ou privado.
     * @return O grupo criado.
     * @throws IllegalArgumentException Se já existir um grupo com o mesmo nome.
     */
    public Group addCustomGroup(Long id, String name, String publicOrPrivate) {
        boolean groupExists = groupRepository.findAll().stream().anyMatch(group ->
                group.getName().equalsIgnoreCase(name)
        );

        if (groupExists) {
            throw new IllegalArgumentException("Já existe um grupo com o mesmo nome, endereço ou porta.");
        }

        String multicastBaseAddress = "230.0.0.";
        int multicastStartPort = 4446;

        Set<String> usedAddresses = groupRepository.findAll().stream()
                .map(Group::getAddress)
                .collect(Collectors.toSet());

        Set<Integer> usedPorts = groupRepository.findAll().stream()
                .map(Group::getPort)
                .collect(Collectors.toSet());

        String newAddress = generateNextAddress(multicastBaseAddress, usedAddresses);
        int newPort = generateNextPort(multicastStartPort, usedPorts);

        boolean isPublic = publicOrPrivate.equalsIgnoreCase("public");

        Group newGroup = new Group();
        newGroup.setName(name);
        newGroup.setAddress(newAddress);
        newGroup.setPort(newPort);
        newGroup.setCreatedBy(id);
        newGroup.setPublic(isPublic);
        newGroup.setRequiredPermissions(Permissions.NO_LEVEL);

        Group savedGroup = groupRepository.save(newGroup);

        System.out.println("Grupo personalizado '" + name + "' criado com sucesso!");

        return savedGroup;
    }

    /**
     * Remove um utilizador de um grupo.
     * Se o grupo tiver apenas um membro, ele será excluído.
     * Caso o utilizador que saiu do grupo seja o criador do mesmo, este cargo será ocupado pelo próximo membro do grupo.
     *
     * @param user Utilizador a ser removido.
     * @param group Grupo do qual o utilizador será removido.
     */
    @Transactional
    public void leaveGroup(User user, Group group) {
        group = groupRepository.findById(group.getId()).orElseThrow(() -> new RuntimeException("Group not found"));
        int groupElements = group.getUsers().size();

        if (groupElements <= 1) {
            groupRepository.delete(group);
        } else {
            List<User> initial = group.getUsers();
            initial.remove(user);
            group.setUsers(initial);

            if (user.getId().equals(group.getCreatedBy())) {
                group.setCreatedBy(group.getUsers().get(1).getId());
            }
            groupRepository.save(group);
            System.out.println("Utilizador removido do grupo: " + group.getName());
        }
    }

    /**
     * Remove um utilizador de todos os grupos públicos onde as suas permissões são insuficientes.
     *
     * @param user Utilizador a ser removido.
     * @param newPermissions Novas permissões do utilizador.
     */
    @Transactional
    public void removeUserFromGroup(User user, Permissions newPermissions) {
        List<Group> publicGroups = groupRepository.findByisPublic(true);
        for (Group group : publicGroups) {
            if (Permissions.fromPermissions(group.getRequiredPermissions()) > Permissions.fromPermissions(newPermissions) && group.getUsers().contains(user)) {
                group.getUsers().remove(user);
                groupRepository.save(group);
                System.out.println("Utilizador removido do grupo: " + group.getName());
            }
        }
    }

    /**
     * Gera o próximo endereço multicast disponível.
     *
     * @param baseAddress Endereço base.
     * @param usedAddresses Conjunto de endereços já em uso.
     * @return O próximo endereço disponível.
     * @throws RuntimeException Se não houver endereços disponíveis.
     */
    private String generateNextAddress(String baseAddress, Set<String> usedAddresses) {
        for (int i = 0; i < 256; i++) {
            String candidateAddress = baseAddress + i;
            if (!usedAddresses.contains(candidateAddress)) {
                return candidateAddress;
            }
        }
        throw new RuntimeException("Não há endereços multicast disponíveis.");
    }

    /**
     * Gera a próxima porta multicast disponível.
     *
     * @param startPort Porta inicial.
     * @param usedPorts Conjunto de portas já em uso.
     * @return A próxima porta disponível.
     * @throws RuntimeException Se não houver portas disponíveis.
     */
    private int generateNextPort(int startPort, Set<Integer> usedPorts) {
        for (int port = startPort; port <= 65535; port++) {
            if (!usedPorts.contains(port)) {
                return port;
            }
        }
        throw new RuntimeException("Não há portas disponíveis.");
    }
}
