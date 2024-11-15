package org.estg.ipp.pt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/user_management";
        String username = "Leo";  // Substitua pelo seu nome de usuário
        String password = "2407Jylho2004_";  // Substitua pela sua senha

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Conexão bem-sucedida ao banco de dados 'users_management'!");

            // Teste uma consulta simples
            Statement stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao banco de dados.");
        }
    }
}
