package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;
import org.estg.ipp.pt.Classes.Enum.TagType;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime = LocalDateTime.now();
    private TagType tag;
    private String message;

    // Getters e setters
    // Construtor para inicialização

    public Long getId() {
        return id;
    }

    public String getDateTime() {
        return dateTime.toString();
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public TagType getTag() {
        return tag;
    }

    public void setTag(TagType tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
