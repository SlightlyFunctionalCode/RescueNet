package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Repositories.GroupRepository;
import org.estg.ipp.pt.Repositories.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    public void initializeDefaultGroups() {
        if (groupRepository.count() == 0) {
            // Criação dos grupos base
            Group groupGeral = new Group();
            groupGeral.setName("GERAL");
            groupGeral.setAddress("230.0.0.0");
            groupGeral.setPort(4446);
            groupGeral.setPublic(false);
            groupGeral.setRequiredPermissions(Permissions.NO_LEVEL);

            Group groupLowLevel = new Group();
            groupLowLevel.setName("LOW_LEVEL");
            groupLowLevel.setAddress("230.0.0.1");
            groupLowLevel.setPort(4447);
            groupLowLevel.setPublic(false);
            groupLowLevel.setRequiredPermissions(Permissions.LOW_LEVEL);

            Group groupMidiumLevel = new Group();
            groupMidiumLevel.setName("MIDIUM_LEVEL");
            groupMidiumLevel.setAddress("230.0.0.2");
            groupMidiumLevel.setPort(4448);
            groupMidiumLevel.setPublic(false);
            groupMidiumLevel.setRequiredPermissions(Permissions.MEDIUM_LEVEL);

            Group groupHighLevel = new Group();
            groupHighLevel.setName("HIGH_LEVEL");
            groupHighLevel.setAddress("230.0.0.3");
            groupHighLevel.setPort(4449);
            groupHighLevel.setPublic(false);
            groupHighLevel.setRequiredPermissions(Permissions.HIGH_LEVEL);

            // Salvando os grupos no repositório
            groupRepository.save(groupGeral);
            groupRepository.save(groupLowLevel);
            groupRepository.save(groupMidiumLevel);
            groupRepository.save(groupHighLevel);

            System.out.println("Grupos base criados com sucesso!");
        } else {
            System.out.println("Grupos base já existem no sistema.");
        }
    }

    @Transactional
    public void addUserToGroup(String groupName, User user) {

        // Carregar o grupo e verificar se ele existe
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Verificar se o usuário já está no grupo usando uma consulta ao banco
        if (groupRepository.existsByIdAndUsersId(group.getId(), user.getId())) {
            System.out.println("O usuário já está no grupo.");
            return;
        }

            // Adicionar o usuário ao grupo
            group.getUsers().add(user);
            user.setCurrentGroup(group);

            // Salvar apenas o grupo (o relacionamento bidirecional será tratado automaticamente)
            groupRepository.save(group);
            System.out.println("Usuário adicionado ao grupo com sucesso!");
    }

    @Transactional
    public Group getUserGroupByNameAndVerify(Long userId, String groupName) {
        // Buscar o grupo pelo nome
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));

        User verifyUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User não existe"));

        if(group.isPublic() && Permissions.fromPermissions(group.getRequiredPermissions()) < Permissions.fromPermissions(verifyUser.getPermissions())) {
            addUserToGroup(group.getName(), verifyUser);
        }

        // Verificar se o usuário pertence ao grupo
        boolean userInGroup = group.getUsers().stream().anyMatch(user -> user.getId().equals(userId));

        if (!userInGroup) {
            throw new IllegalArgumentException("Usuário com ID " + userId + " não pertence ao grupo " + groupName);
        }

        // Retornar o grupo se a verificação passar
        return group;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll(); // Retorna todos os grupos no banco de dados
    }

    public List<User> getUsersByGroupName(String groupName) {
        // Verifica se o grupo existe
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        // Retorna a lista de usuários associados ao grupo
        return new ArrayList<>(group.getUsers());
    }

    @Transactional
    public boolean isUserInGroup(String groupName, Long userId) {
        // Buscar o grupo pelo nome
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));

        // Verificar se o usuário pertence ao grupo
        return group.getUsers().stream().anyMatch(user -> user.getId().equals(userId));
    }

    public Group getGroupByName(String groupName) {

        return groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));
    }

    public Group addCustomGroup(Long id, String name, String publicOrPrivate) {
        // Verifica se já existe um grupo com o mesmo nome, endereço ou porta
        boolean groupExists = groupRepository.findAll().stream().anyMatch(group ->
                group.getName().equalsIgnoreCase(name)
        );

        if (groupExists) {
            throw new IllegalArgumentException("Já existe um grupo com o mesmo nome, endereço ou porta.");
        }

        String multicastBaseAddress = "230.0.0.";
        int multicastStartPort = 4446;

        // Obter endereços e portas existentes
        Set<String> usedAddresses = groupRepository.findAll().stream()
                .map(Group::getAddress)
                .collect(Collectors.toSet());

        Set<Integer> usedPorts = groupRepository.findAll().stream()
                .map(Group::getPort)
                .collect(Collectors.toSet());

        // Gerar próximo endereço e porta disponíveis
        String newAddress = generateNextAddress(multicastBaseAddress, usedAddresses);
        int newPort = generateNextPort(multicastStartPort, usedPorts);

        boolean isPublic = publicOrPrivate.equalsIgnoreCase("public");

        // Criação do novo grupo
        Group newGroup = new Group();
        newGroup.setName(name);
        newGroup.setAddress(newAddress);
        newGroup.setPort(newPort);
        newGroup.setCreatedBy(id);
        newGroup.setPublic(isPublic);
        newGroup.setRequiredPermissions(Permissions.NO_LEVEL);

        // Salvar o grupo no repositório
        Group savedGroup = groupRepository.save(newGroup);

        System.out.println("Grupo personalizado '" + name + "' criado com sucesso!");

        // Retorna o grupo criado
        return savedGroup;
    }

    @Transactional
    public void removeUserFromRestrictedGroups(User user, Permissions newPermissions) {
        // Buscar todos os grupos privados
        List<Group> privateGroups = groupRepository.findByisPublic(false);

        // Iterar pelos grupos e verificar permissões
        for (Group group : privateGroups) {
            if (Permissions.fromPermissions(group.getRequiredPermissions()) > Permissions.fromPermissions(newPermissions)) {
                // Remover o usuário do grupo
                group.getUsers().remove(user);
                groupRepository.save(group);
                System.out.println("Usuário removido do grupo: " + group.getName());
            }
        }
    }

    public String getGroupNameById(Long groupId) {
        // Buscar o grupo pelo ID
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com ID " + groupId + " não encontrado"));

        // Retornar o nome do grupo
        return group.getName();
    }

    private String generateNextAddress(String baseAddress, Set<String> usedAddresses) {
        for (int i = 0; i < 256; i++) {
            String candidateAddress = baseAddress + i;
            if (!usedAddresses.contains(candidateAddress)) {
                return candidateAddress;
            }
        }
        throw new RuntimeException("Não há endereços multicast disponíveis.");
    }

    private int generateNextPort(int startPort, Set<Integer> usedPorts) {
        for (int port = startPort; port <= 65535; port++) {
            if (!usedPorts.contains(port)) {
                return port;
            }
        }
        throw new RuntimeException("Não há portas disponíveis.");
    }

    public int getAvailablePort() throws IOException {
        try (ServerSocket tempSocket = new ServerSocket(0)) { // Bind to an available port
            tempSocket.setReuseAddress(true);
            return tempSocket.getLocalPort(); // Return the port number
        }
    }
}
