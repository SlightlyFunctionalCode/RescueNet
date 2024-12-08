package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.estg.ipp.pt.Classes.Enum.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private Permissions permission;
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_group_id")  // Define o nome da coluna da FK na tabela de usuários
    private Group currentGroup;


    // Getters e setters
    // Construtor para inicialização

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Permissions getPermissions() {
        return permission;
    }

    public void setPermissions(Permissions permission) {
        this.permission = permission;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Group getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(Group currentGroup) {
        this.currentGroup = currentGroup;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", permission=" + permission +
                ", password='" + password + '\'' +
                ", currentGroup=" + currentGroup +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
