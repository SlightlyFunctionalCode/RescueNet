package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Repositories.GroupRepository;
import org.estg.ipp.pt.Repositories.UserRepository;
import org.hibernate.Hibernate;
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
            user.getGroups().add(group);

            // Salvar apenas o grupo (o relacionamento bidirecional será tratado automaticamente)
            groupRepository.save(group);
            System.out.println("Usuário adicionado ao grupo com sucesso!");
    }

    @Transactional
    public Group getUserGroupByName(Long userId, String groupName) {
        // Buscar o usuário do banco de dados
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Inicializar a coleção de grupos (se necessário)
        Hibernate.initialize(user.getGroups());

        // Procurar o grupo na coleção do usuário
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

    @Transactional
    public boolean isUserInGroup(String groupName, Long userId) {
        System.out.println(groupName);
        // Buscar o grupo pelo nome
        Group group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        // Buscar o usuário pelo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Verificar se o usuário está associado ao grupo
        return group.getUsers().contains(user);
    }

    public Group addCustomGroup(Long id, String name, String address, String port) {
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
        newGroup.setCreatedBy(id);

        // Salvar o grupo no repositório
        Group savedGroup = groupRepository.save(newGroup);

        System.out.println("Grupo personalizado '" + name + "' criado com sucesso!");

        // Retorna o grupo criado
        return savedGroup;
    }




}
