package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Repositories.GroupRepository;
import org.estg.ipp.pt.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

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
            groupGeral.setPort("4446");

            Group groupLowLevel = new Group();
            groupLowLevel.setName("LOW_LEVEL");
            groupLowLevel.setAddress("230.0.0.1");
            groupLowLevel.setPort("4447");

            Group groupMidiumLevel = new Group();
            groupMidiumLevel.setName("MIDIUM_LEVEL");
            groupMidiumLevel.setAddress("230.0.0.2");
            groupMidiumLevel.setPort("4448");

            Group groupHighLevel = new Group();
            groupHighLevel.setName("HIGH_LEVEL");
            groupHighLevel.setAddress("230.0.0.3");
            groupHighLevel.setPort("4449");

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

    public void addUserToGroup(String groupName, Long userId) {
        // Carregar o grupo e o usuário, garantindo que ambos existem na base de dados
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        System.out.println("Grupo encontrado: " + group.getName());
        // Certifique-se de que o usuário e o grupo não estão já associados
        if (!group.getUsers().contains(user)) {
            group.getUsers().add(user);
            user.getGroups().add(group);

            // Salvar as alterações manualmente
            groupRepository.save(group); // Salva o grupo com o novo usuário
            userRepository.save(user);   // Salva o usuário com o novo grupo
        } else {
            System.out.println("O usuário já está no grupo.");
        }
    }

    public Group getUserGroupByName(Long userId, String groupName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return user.getGroups().stream()
                .filter(group -> group.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Grupo com nome " + groupName + " não encontrado para o usuário"));
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

    public boolean isUserInGroup(String groupName, Long userId) {
        // Buscar o grupo pelo nome
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        // Buscar o usuário pelo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Verificar se o usuário está associado ao grupo
        return group.getUsers().contains(user);
    }

    public Group addCustomGroup(String name, String address, String port) {
        // Verifica se já existe um grupo com o mesmo nome, endereço ou porta
        boolean groupExists = groupRepository.findAll().stream().anyMatch(group ->
                group.getName().equalsIgnoreCase(name) ||
                        group.getAddress().equals(address) ||
                        group.getPort().equals(port)
        );

        if (groupExists) {
            throw new IllegalArgumentException("Já existe um grupo com o mesmo nome, endereço ou porta.");
        }

        // Criação do novo grupo
        Group newGroup = new Group();
        newGroup.setName(name);
        newGroup.setAddress(address);
        newGroup.setPort(port);

        // Salvar o grupo no repositório
        Group savedGroup = groupRepository.save(newGroup);

        System.out.println("Grupo personalizado '" + name + "' criado com sucesso!");

        // Retorna o grupo criado
        return savedGroup;
    }


}
