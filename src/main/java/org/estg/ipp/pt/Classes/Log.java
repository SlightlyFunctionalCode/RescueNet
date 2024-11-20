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

    // No-argument constructor (required by JPA)
    public Log() {
    }

    // Parameterized constructor for easy initialization
    public Log(LocalDateTime dateTime, TagType tag, String message) {
        this.dateTime = dateTime;
        this.tag = tag;
        this.message = message;
    }

    // Getters and setters
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
