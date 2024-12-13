package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;
import org.estg.ipp.pt.Classes.Enum.Permissions;

import java.util.ArrayList;
import java.util.List;

/**
 * A classe {@code Group} representa um grupo de multicast, e contém as informações sobre o grupo, como o nome,
 * o endereço, a porta, a privacidade e as permissões necessárias.
 * Também mantém a lista de utilizadores associados ao grupo.
 *
 * <p>A classe mapeia a tabela {@code groups} na base de dados e define um relacionamento {@code ManyToMany} com a
 * classe {@code User}, através da tabela intermediária {@code group_user}.</p>
 */
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private int port;
    private boolean isPublic;
    private Permissions requiredPermissions;
    private Long createdBy;

    /**
     * Lista de utilizadores associados ao grupo.
     * A relação é {@code ManyToMany} com a classe {@code User}, e a tabela intermediária é {@code group_user}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_user",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users = new ArrayList<>();

    /**
     * Devolve o identificador do grupo.
     *
     * @return O id do grupo.
     */
    public Long getId() {
        return id;
    }

    /**
     * Devolve o nome do grupo.
     *
     * @return O nome do grupo.
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do grupo.
     *
     * @param name O nome do grupo.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Devolve o endereço do grupo.
     *
     * @return O endereço do grupo.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Define o endereço do grupo.
     *
     * @param address O endereço do grupo.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Devolve a porta associada ao grupo.
     *
     * @return A porta do grupo.
     */
    public int getPort() {
        return port;
    }

    /**
     * Define a porta associada ao grupo.
     *
     * @param port A porta do grupo.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Devolve o identificador do utilizador que criou o grupo.
     *
     * @return O id do criador do grupo.
     */
    public Long getCreatedBy() {
        return createdBy;
    }

    /**
     * Define o identificador do utilizador que criou o grupo.
     *
     * @param createdBy O id do criador do grupo.
     */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Devolve a lista de utilizadores associados ao grupo.
     *
     * @return A lista de utilizadores do grupo.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Define a lista de utilizadores associados ao grupo.
     *
     * @param users A lista de utilizadores.
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Define o identificador único do grupo.
     *
     * @param id O id do grupo.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Devolve se o grupo é público ou privado.
     *
     * @return {@code true} se o grupo for público, {@code false} caso contrário.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Define a privacidade do grupo.
     *
     * @param aPublic {@code true} se o grupo for público, {@code false} caso contrário.
     */
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    /**
     * Devolve as permissões necessárias para se fazer parte do grupo.
     *
     * @return As permissões necessárias.
     */
    public Permissions getRequiredPermissions() {
        return requiredPermissions;
    }

    /**
     * Define as permissões necessárias para fazer parte do grupo.
     *
     * @param requiredPermissions As permissões necessárias.
     */
    public void setRequiredPermissions(Permissions requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }
}
