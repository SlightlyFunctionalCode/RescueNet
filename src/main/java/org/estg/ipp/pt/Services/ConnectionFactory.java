package org.estg.ipp.pt.Services;

import java.sql.*;

public class ConnectionFactory {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/user_management";
    private static final String USER = "Leo";
    private static final String PASS = "2407Jylho2004_";

    public static Connection getConnection() {
        try{
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection con) {
        try{
            if(con != null) {
                con.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection con, PreparedStatement stmt) {
        closeConnection(con);

        try{
            if(stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection con, PreparedStatement stmt, ResultSet rs) {
        closeConnection(con, stmt);

        try{
            if(rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
