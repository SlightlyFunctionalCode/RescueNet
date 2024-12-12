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
            groupGeral.setPublic(true);
            groupGeral.setRequiredPermissions(Permissions.NO_LEVEL);

            Group groupLowLevel = new Group();
            groupLowLevel.setName("LOW_LEVEL");
            groupLowLevel.setAddress("230.0.0.1");
            groupLowLevel.setPort(4447);
            groupLowLevel.setPublic(true);
            groupLowLevel.setRequiredPermissions(Permissions.LOW_LEVEL);

            Group groupMidiumLevel = new Group();
            groupMidiumLevel.setName("MEDIUM_LEVEL");
            groupMidiumLevel.setAddress("230.0.0.2");
            groupMidiumLevel.setPort(4448);
            groupMidiumLevel.setPublic(true);
            groupMidiumLevel.setRequiredPermissions(Permissions.MEDIUM_LEVEL);

            Group groupHighLevel = new Group();
            groupHighLevel.setName("HIGH_LEVEL");
            groupHighLevel.setAddress("230.0.0.3");
            groupHighLevel.setPort(4449);
            groupHighLevel.setPublic(true);
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

    public List<Group> getAllGroups() {
        return groupRepository.findAll(); // Retorna todos os grupos no banco de dados
    }

    public List<User> getUsersByGroupName(String groupName) {
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        return new ArrayList<>(group.getUsers());
    }

    @Transactional
    public boolean isUserInGroup(String groupName, Long userId) {
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));

        return group.getUsers().stream().anyMatch(user -> user.getId().equals(userId));
    }

    public Group getGroupByName(String groupName) throws IllegalArgumentException {

        return groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado"));
    }

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
}
