package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Channel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String type; // Tipo de operação (e.g., Evacuação, Comunicações de Emergência)

        @OneToMany
        private List<User> participants;

        // Getters e setters
}

