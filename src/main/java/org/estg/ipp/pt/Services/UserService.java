package org.estg.ipp.pt.Services;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.ConnectionFactory;
import org.estg.ipp.pt.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService  {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void register(User user) {
        System.out.println("Registering user: " + user.getName());
        Connection con = ConnectionFactory.getConnection();
        PreparedStatement ps = null;
        try {
            // Encode the password before storing it
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            String sql = "INSERT INTO User (identifier, name, profile, password) VALUES (?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, user.getIdentifier());
            ps.setString(2, user.getName());
            ps.setString(3, user.getProfile());
            ps.setString(4, user.getPassword());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User registered successfully");
            } else {
                System.out.println("User registration failed");
            }
        } catch (SQLException ex) {
            System.out.println("Error while registering user: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            ConnectionFactory.closeConnection(con, ps);
        }
    }

    public User authenticate(String identifier, String password) {
        Connection con = ConnectionFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;
        return null;
    }
}
