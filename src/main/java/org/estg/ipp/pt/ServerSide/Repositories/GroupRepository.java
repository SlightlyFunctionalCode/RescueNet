package org.estg.ipp.pt.ServerSide.Repositories;

import org.estg.ipp.pt.Classes.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface de repositório para a entidade {@link Group}.
 * Esta interface estende o {@link JpaRepository} e fornece os métodos para realizar as operações CRUD
 * básicas sobre o repositório de grupos. Além disso, fornece as consultas personalizadas para encontrar
 * grupos com base nos seus atributos.
 *
 * <p>A interface {@link GroupRepository} permite:</p>
 * <ul>
 *     <li>Encontrar um grupo pelo nome.</li>
 *     <li>Verificar a associação de um grupo com um utilizador pelo ID.</li>
 *     <li>Buscar grupos com base na visibilidade pública.</li>
 * </ul>
 *
 * @see Group
 * @see JpaRepository
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Encontra um grupo pelo seu nome.
     *
     * @param name Nome do grupo a ser encontrado.
     * @return Um {@link Optional} que contém o grupo encontrado ou vazio caso não haja nenhum grupo com o nome fornecido.
     */
    Optional<Group> findByName(String name);

    /**
     * Verifica se existe um grupo com o ID fornecido que contenha o utilizador com o ID fornecido.
     *
     * @param id ID do grupo.
     * @param usersId ID do utilizador.
     * @return {@code true} se o grupo com o ID fornecido contiver o utilizador com o ID fornecido, caso contrário {@code false}.
     */
    boolean existsByIdAndUsersId(Long id, Long usersId);

    /**
     * Encontra todos os grupos que têm o campo {@code isPublic} com o valor fornecido.
     *
     * @param isPublic Indica se o grupo deve ser público ou não.
     * @return Uma lista de grupos que correspondem ao valor fornecido para {@code isPublic}.
     */
    List<Group> findByisPublic(boolean isPublic);
}
