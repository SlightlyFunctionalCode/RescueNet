package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String port;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<User> users = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }


    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
