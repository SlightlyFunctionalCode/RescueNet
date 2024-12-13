package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;
import org.estg.ipp.pt.Classes.Enum.TagType;

import java.time.LocalDateTime;

/**
 * A classe {@code Log} representa uma entrada de ‘log’ no sistema, que contém as informações sobre a data e hora,
 * o tipo de tag e a mensagem associada ao ‘log’.
 *
 * <p>Esta classe está mapeada para a tabela {@code logs} na base de dados.</p>
 */
@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime = LocalDateTime.now();
    private TagType tag;
    private String message;

    /**
     * Construtor sem argumentos.
     * Obrigatório pelo JPA para instanciar os objetos da entidade.
     */
    public Log() {
    }

    /**
     * Construtor com parâmetros para a inicialização mais fácil de ‘logs’.
     *
     * @param dateTime A data e hora do log.
     * @param tag      O tipo de tag do log.
     * @param message  A mensagem do ‘log’.
     */
    public Log(LocalDateTime dateTime, TagType tag, String message) {
        this.dateTime = dateTime;
        this.tag = tag;
        this.message = message;
    }

    /**
     * Retorna o identificador único do log.
     *
     * @return O id do log.
     */
    public Long getId() {
        return id;
    }

    /**
     * Devolve a data e a hora do ‘log’ como uma string formatada.
     *
     * @return A data e hora do log.
     */
    public String getDateTime() {
        return dateTime.toString();
    }

    /**
     * Define a data e hora do log.
     *
     * @param dateTime A data e hora do log.
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Devolve o tipo de tag do log.
     *
     * @return O tipo de tag do log.
     */
    public TagType getTag() {
        return tag;
    }

    /**
     * Define o tipo de tag do log.
     *
     * @param tag O tipo de tag.
     */
    public void setTag(TagType tag) {
        this.tag = tag;
    }

    /**
     * Devolve a mensagem associada ao log.
     *
     * @return A mensagem do log.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Define a mensagem associada ao log.
     *
     * @param message A mensagem do log.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
