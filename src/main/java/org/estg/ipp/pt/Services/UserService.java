package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
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

import static org.estg.ipp.pt.Classes.Enum.Permissions.fromPermissions;

@Service
public class UserService {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public int register(User user) {
        System.out.println("Registering user: " + user.getName());
        Connection con = ConnectionFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            //Verificar se o email ou nome de utilizador já existe no sistema

            String checkNameQuery = "SELECT COUNT(*) FROM users WHERE name = ?";
            ps = con.prepareStatement(checkNameQuery);
            ps.setString(1, user.getName());
            rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Username already exists");
                return 0;
            }

            String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            ps = con.prepareStatement(checkEmailQuery);
            ps.setString(1, user.getEmail());
            rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Email already exists");
                return 0;
            }
            System.out.println(user.getEmail());
            // Encode the password before storing it
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            String sql = "INSERT INTO users (name, email, permission, password) VALUES (?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, fromPermissions(user.getPermissions()));
            ps.setString(4, user.getPassword());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User registered successfully");
                return 1;
            } else {
                System.out.println("User registration failed");
            }
        } catch (SQLException ex) {
            System.out.println("Error while registering user: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            ConnectionFactory.closeConnection(con, ps);
        }
        return 0;
    }

    public User authenticate(String usernameOremail, String password) {
        Connection con = ConnectionFactory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            String sql = "SELECT * FROM users WHERE name = ? OR email = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, usernameOremail);
            ps.setString(2, usernameOremail);
            rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                System.out.println("Stored Password: " + storedPassword);
                System.out.println("Provided Password: " + password);

                if (passwordEncoder.matches(password, storedPassword)) {
                    // Se a senha estiver correta, cria o objeto User
                    user = new User();
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(storedPassword);
                    user.setPermissions(Permissions.fromValue(rs.getInt("permission")));
                }else{
                    System.out.println("Incorrect password");
                }
            }else{
                System.out.println("User not found");
            }
        } catch (SQLException ex) {
            System.out.println("Error while authenticating user: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            ConnectionFactory.closeConnection(con, ps);
        }
        return user; // Retorna o objeto User ou null se não encontrado
    }
}
