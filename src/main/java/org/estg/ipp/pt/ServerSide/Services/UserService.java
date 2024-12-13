package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.ServerSide.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço para gerir os utilizadores no sistema.
 *
 * <p>Este serviço oferece as funcionalidades para registar, autenticar, atualizar permissões e
 * gerir os grupos de utilizadores. Ele também garante que o utilizador padrão "admin" seja
 * inicializado caso este não exista.</p>
 *
 * <p><b>Funcionalidades principais:</b></p>
 * <ol>
 *   <li>Inicialização do utilizador "admin" se ele não existir.</li>
 *   <li>Registo de um novo utilizador com validações para garantir a unicidade do nome e email.</li>
 *   <li>Autenticação de utilizadores com base em nome de utilizador ou email e senha.</li>
 *   <li>Recuperação de utilizadores por nome.</li>
 *   <li>Associação de utilizadores a grupos.</li>
 *   <li>Atualização de permissões de utilizadores.</li>
 * </ol>
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);


    /**
     * Inicializa o utilizador padrão "admin" caso ele não exista no sistema.
     *
     * <p>Este método verifica se o utilizador "admin" já existe e, caso contrário,
     * cria e salva o utilizador com permissões de nível alto.</p>
     */
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
     * Regista um novo utilizador no sistema.
     *
     * <p>Este método valida se o nome de utilizador ou o email já existem. Se não, ele
     * cria o utilizador com a senha encriptada e o guarda na base de dados.</p>
     *
     * @param user O utilizador a ser registado.
     * @return 1 se o utilizador foi registado com sucesso, 0 se o nome de utilizador ou email
     * já existirem no sistema.
     */
    public int register(User user) {
        if (userRepository.existsByName(user.getName())) {
            System.out.println("Username already exists");
            return 0;
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            System.out.println("Email already exists");
            return 0;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        System.out.println("User registered successfully");
        return 1;
    }

    /**
     * Autentica um utilizador com base no nome de utilizador ou email e senha.
     *
     * <p>Este método busca o utilizador pelo nome ou email. Se encontrado, verifica se a senha
     * fornecida corresponde à senha armazenada. Se a autenticação for bem-sucedida, devolve o
     * utilizador, caso contrário, devolve null.</p>
     *
     * @param user O utilizador que se pretende autenticar.
     * @param password A senha fornecida pelo utilizador.
     * @return O utilizador autenticado, ou null se a autenticação falhar.
     */
    public boolean authenticate(User user, String password) {
        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("Authentication successful");
            return true;
        } else {
            System.out.println("Invalid password");
        }
        return false;
    }

    /**
     * Recupera um utilizador pelo nome de utilizador ou email.
     *
     * <p>Este método devolve o utilizador associado ao nome ou email fornecido, ou null se o utilizador
     * não for encontrado.</p>
     *
     * @param usernameOrEmail O nome ou email do utilizador a ser recuperado.
     * @return O utilizador correspondente ao nome ou email, ou null se não encontrado.
     */
    public User getUserByNameOrEmail(String usernameOrEmail) {
        Optional<User> userOptional = userRepository.findByEmailOrName(usernameOrEmail);

        return userOptional.orElse(null);
    }

    /**
     * Recupera um utilizador pelo nome de utilizador.
     *
     * <p>Este método devolve o utilizador associado ao nome fornecido, ou null se o utilizador
     * não for encontrado.</p>
     *
     * @param username O nome de utilizador do utilizador a ser recuperado.
     * @return O utilizador correspondente ao nome, ou null se não encontrado.
     */
    public User getUserByName(String username) {
        Optional<User> user = userRepository.findByName(username);
        return user.orElse(null);
    }

    /**
     * Associa um utilizador a um grupo.
     *
     * <p>Este método verifica se o utilizador já pertence ao grupo fornecido. Caso contrário,
     * ele associa o utilizador ao grupo e guarda as alterações.</p>
     *
     * @param user  O utilizador a ser associado ao grupo.
     * @param group O grupo ao qual o utilizador será associado.
     * @throws IllegalArgumentException Se o utilizador já pertence ao grupo.
     */
    public void joinGroup(User user, Group group) throws IllegalArgumentException {
        if (user.getCurrentGroup() != null && user.getCurrentGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("Utilizador já pertence ao grupo " + group.getName());
        }

        user.setCurrentGroup(group);

        userRepository.save(user);
    }

    /**
     * Atualiza as permissões de um utilizador.
     *
     * <p>Este método busca o utilizador pelo nome e, se encontrado, atualiza as suas permissões
     * com o valor fornecido. Devolve true se a atualização foi bem-sucedida e false se o
     * utilizador não foi encontrado.</p>
     *
     * @param username       O nome do utilizador cujas permissões serão atualizadas.
     * @param newPermissions As novas permissões a serem atribuídas ao utilizador.
     */
    public void updateUserPermissions(String username, Permissions newPermissions) {
        Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            user.setPermissions(newPermissions);

            userRepository.save(user);

            System.out.println("Permissões do utilizador atualizadas com sucesso.");
        } else {
            System.out.println("Utilizador não encontrado.");
        }
    }
}
