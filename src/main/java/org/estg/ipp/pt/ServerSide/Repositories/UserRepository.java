package org.estg.ipp.pt.ServerSide.Repositories;

import org.estg.ipp.pt.Classes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de repositório para a entidade {@link User}.
 *
 * Esta interface estende o {@link JpaRepository}, oferecendo métodos para realizar operações CRUD
 * básicas sobre o repositório de usuários. Além disso, fornece consultas personalizadas para encontrar
 * usuários com base em nome ou e-mail, bem como verificar a existência de usuários com esses atributos.
 *
 * <p>A interface {@link UserRepository} permite:</p>
 * <ul>
 *     <li>Buscar um usuário pelo seu nome.</li>
 *     <li>Buscar um usuário pelo seu e-mail.</li>
 *     <li>Verificar se um usuário existe com um nome específico.</li>
 *     <li>Verificar se um usuário existe com um e-mail específico.</li>
 * </ul>
 *
 * <p>O repositório utiliza o Spring Data JPA para facilitar o acesso ao banco de dados e permitir a execução de consultas.</p>
 *
 * @see User
 * @see JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Encontra um usuário pelo seu nome.
     *
     * @param name O nome do usuário.
     * @return Um {@link Optional} contendo o usuário encontrado, ou vazio se o usuário não for encontrado.
     */
    Optional<User> findByName(String name);

    /**
     * Encontra um usuário pelo seu e-mail.
     *
     * @param email O e-mail do usuário.
     * @return Um {@link Optional} contendo o usuário encontrado, ou vazio se o usuário não for encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se um usuário existe com o nome fornecido.
     *
     * @param name O nome do usuário.
     * @return {@code true} se um usuário com o nome fornecido existir, caso contrário, {@code false}.
     */
    boolean existsByName(String name);

    /**
     * Verifica se um usuário existe com o e-mail fornecido.
     *
     * @param email O e-mail do usuário.
     * @return {@code true} se um usuário com o e-mail fornecido existir, caso contrário, {@code false}.
     */
    boolean existsByEmail(String email);
}

