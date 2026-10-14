package com.desarrollo.proyectocrudg3.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/bdventas_1";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    private ConexionDB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}