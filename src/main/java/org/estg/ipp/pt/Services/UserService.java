package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Repositories.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Permission;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // Ensure this is injected correctly by Spring
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);


    public void initializeUser() {
        if (!userRepository.existsByName("admin")) {
            User user = new User();
            user.setName("admin");
            user.setPassword(passwordEncoder.encode("admin"));
            user.setEmail("admin@admin.admin");
            user.setPermissions(Permissions.HIGH_LEVEL);

            userRepository.save(user);
        }
    }
    /**
     * Registers a new user.
     *
     * @param user The user to register.
     * @return 1 if the user was registered successfully, 0 if the username or email already exists.
     */
    public int register(User user) {
        // Check if the username already exists
        if (userRepository.existsByName(user.getName())) {
            System.out.println("Username already exists");
            return 0;
        }

        // Check if email already exists (requires an additional repository method)
        if (userRepository.existsByEmail(user.getEmail())) { // Make sure to define this method in the repository
            System.out.println("Email already exists");
            return 0;
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user in the database
        userRepository.save(user);
        System.out.println("User registered successfully");
        return 1;
    }

    /* TODO: Melhorar o método*/
    /**
     * Authenticates a user by username or email and password.
     *
     * @param usernameOrEmail The username or email of the user.
     * @param password        The password to validate.
     * @return The authenticated user, or null if authentication fails.
     */
    public User authenticate(String usernameOrEmail, String password) {
        // Find user by name
        Optional<User> userOptional = userRepository.findByName(usernameOrEmail);

        // If not found by name, check by email (requires an additional repository method)
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(usernameOrEmail); // Define findByEmail in the repository
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the provided password matches the stored password
            if (passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("Authentication successful");
                return user;
            } else {
                System.out.println("Invalid password");
            }
        } else {
            System.out.println("User not found");
        }
        return null;
    }

    public User getUserByName(String username) {
        Optional<User> user = userRepository.findByName(username);
        return user.orElse(null);
    }

    public void joinGroup(User user, Group group) {
        // Verifica se o usuário já pertence ao grupo
        if (user.getCurrentGroup() != null && user.getCurrentGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("Usuário já pertence ao grupo " + group.getName());
        }

        // Atualiza o grupo atual do usuário
        user.setCurrentGroup(group);

        // Adiciona qualquer outra lógica ou ajuste que você precise fazer quando o usuário entrar no grupo
        // Exemplo: Atribuir permissões, inicializar configurações, etc.

        // O usuário precisa ser salvo, então chama-se o método de persistência aqui
        userRepository.save(user);  // O save do repositório será chamado no final para persistir as mudanças
    }

    /**
     * Updates the permissions of a user.
     *
     * @param username The username of the user whose permissions need to be updated.
     * @param newPermissions The new permissions to assign.
     * @return true if the update was successful, false if the user was not found.
     */
    public boolean updateUserPermissions(String username, Permissions newPermissions) {
        // Busca o utilizador pelo nome
        Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            Permissions previousPermission = user.getPermissions();
            // Atualizar as permissões
            user.setPermissions(newPermissions);

            // Salvar as alterações no banco de dados
            userRepository.save(user);

            System.out.println("Permissões do usuário atualizadas com sucesso.");
            return true;
        } else {
            System.out.println("Usuário não encontrado.");
            return false;
        }
    }
}
