package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.estg.ipp.pt.Classes.Enum.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A classe {@code User} representa um utilizador na aplicação e inclui detalhes
 * como o nome, o email, as permissões e o grupo que o mesmo está atualmente.
 *
 * <p>Esta classe é uma entidade JPA mapeada para uma tabela na base de dados chamada "users".</p>
 */
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

    /**
     * Grupo atual ao qual o utilizador pertence.
     *
     * <p>O mapeamento {@code @ManyToOne} indica uma relação muitos-para-um com a entidade {@code Group}.
     * O fetch {@code FetchType.EAGER} carrega os dados do grupo imediatamente.</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_group_id")
    private Group currentGroup;

    /**
     * Devolve o identificador único do utilizador.
     *
     * @return O id do utilizador.
     */
    public Long getId() {
        return id;
    }

    /**
     * Devolve o endereço de email do utilizador.
     *
     * @return O email do utilizador.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o endereço de email do utilizador.
     *
     * @param email O email a ser definido.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Devolve o nome do utilizador.
     *
     * @return O nome do utilizador.
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do utilizador.
     *
     * @param name O nome a ser definido.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Devolve as permissões do utilizador.
     *
     * @return As permissões do utilizador.
     */
    public Permissions getPermissions() {
        return permission;
    }

    /**
     * Define as permissões do utilizador.
     *
     * @param permission As permissões a serem definidas.
     */
    public void setPermissions(Permissions permission) {
        this.permission = permission;
    }

    /**
     * Devolve a palavra-passe do utilizador.
     *
     * @return A palavra-passe do utilizador.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define a palavra-passe do utilizador.
     *
     * @param password A palavra-passe a ser definida.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Devolve o grupo atual ao qual o utilizador pertence.
     *
     * @return O grupo atual.
     */
    public Group getCurrentGroup() {
        return currentGroup;
    }

    /**
     * Define o grupo atual do utilizador.
     *
     * @param currentGroup O grupo a ser definido.
     */
    public void setCurrentGroup(Group currentGroup) {
        this.currentGroup = currentGroup;
    }

    /**
     * Devolve a representação textual do utilizador.
     *
     * @return Uma string que contém os dados do utilizador.
     */
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

    /**
     * Verifica se os utilizadores comparados são iguais
     *
     * @param o objeto que se pretende comparar
     * @return {@code true} se for igual, caso contrário, {@code false}.
     */
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
