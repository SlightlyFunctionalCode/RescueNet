package org.estg.ipp.pt.ServerSide.Repositories;

import org.estg.ipp.pt.Classes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de repositório para a entidade {@link User}.
 *
 * <p>Esta interface estende o {@link JpaRepository} e oferece os métodos para realizar as operações CRUD
 * básicas sobre o repositório de utilizadores. Além disso, fornece as consultas personalizadas para encontrar
 * utilizadores com base em nome ou e-mail, bem como verificar a existência de utilizadores com esses atributos.</p>
 *
 * <p>A interface {@link UserRepository} permite:</p>
 * <ul>
 *     <li>Buscar um utilizador pelo seu nome.</li>
 *     <li>Buscar um utilizador pelo seu e-mail.</li>
 *     <li>Verificar se um utilizador existe com um nome específico.</li>
 *     <li>Verificar se um utilizador existe com um e-mail específico.</li>
 * </ul>
 *
 * @see User
 * @see JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Encontra um utilizador pelo seu nome.
     *
     * @param name O nome do utilizador.
     * @return Um {@link Optional} que contém o utilizador encontrado, ou vazio se o utilizador não for encontrado.
     */
    Optional<User> findByName(String name);

    /**
     * Encontra um utilizador pelo seu e-mail.
     *
     * @param nameOrEmail O e-mail ou nome do utilizador.
     * @return Um {@link Optional} que contém o utilizador encontrado, ou vazio se o utilizador não for encontrado.
     */
    @Query ("SELECT u from User u WHERE u.name = :nameOrEmail OR u.email = :nameOrEmail")
    Optional<User> findByEmailOrName(String nameOrEmail);

    /**
     * Verifica se um utilizador existe com o nome fornecido.
     *
     * @param name O nome do utilizador.
     * @return {@code true} se um utilizador com o nome fornecido existir, caso contrário, {@code false}.
     */
    boolean existsByName(String name);

    /**
     * Verifica se um utilizador existe com o e-mail fornecido.
     *
     * @param email O e-mail do utilizador.
     * @return {@code true} se um utilizador com o e-mail fornecido existir, caso contrário, {@code false}.
     */
    boolean existsByEmail(String email);
}

