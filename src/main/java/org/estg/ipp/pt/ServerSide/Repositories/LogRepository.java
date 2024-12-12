package org.estg.ipp.pt.ServerSide.Repositories;

import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface de repositório para a entidade {@link Log}.
 *
 * Esta interface estende o {@link JpaRepository}, fornecendo métodos para realizar operações CRUD
 * básicas sobre o repositório de logs. Além disso, fornece consultas personalizadas para encontrar
 * logs com base em critérios específicos como tag e intervalo de datas.
 *
 * <p>A interface {@link LogRepository} permite:</p>
 * <ul>
 *     <li>Buscar logs por uma tag específica.</li>
 *     <li>Buscar logs dentro de um intervalo de datas.</li>
 *     <li>Buscar logs por tag num intervalo de datas.</li>
 * </ul>
 *
 * <p>O repositório utiliza o Spring Data JPA, facilitando o acesso ao banco de dados com consultas personalizadas.</p>
 *
 * @see Log
 * @see JpaRepository
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    /**
     * Encontra logs com uma tag específica.
     *
     * @param tag A tag com a qual os logs devem ser filtrados.
     * @return Uma lista de logs que correspondem à tag fornecida.
     */
    List<Log> findByTag(TagType tag);

    /**
     * Encontra logs num intervalo de datas.
     *
     * @param startDate A data de início do intervalo.
     * @param endDate A data de término do intervalo.
     * @return Uma lista de logs registados dentro do intervalo de datas fornecido.
     */
    @Query("SELECT l FROM Log l WHERE l.dateTime BETWEEN :startDate AND :endDate")
    List<Log> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Encontra logs com uma tag específica num intervalo de datas.
     *
     * @param tag A tag com a qual os logs devem ser filtrados.
     * @param startDate A data de início do intervalo.
     * @param endDate A data de término do intervalo.
     * @return Uma lista de logs que correspondem à tag e ao intervalo de datas fornecido.
     */
    @Query("SELECT l FROM Log l WHERE l.tag = :tag AND l.dateTime BETWEEN :startDate AND :endDate")
    List<Log> findByTagAndDateRange(TagType tag, LocalDateTime startDate, LocalDateTime endDate);
}
