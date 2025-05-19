package com.example.logingui;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private Connection databaseLink;

    public Connection getConnection() {
        if (databaseLink != null) {
            try {
                // Check if connection is still valid
                if (!databaseLink.isClosed() && databaseLink.isValid(5)) {
                    return databaseLink;
                }
            } catch (Exception e) {
                // If there's an error checking the connection, we'll create a new one
                e.printStackTrace();
            }
        }

        String databaseName = "login_db";
        String databaseUser = "root";
        String databasePassword = "mehdi1234";
        String Url = "jdbc:mysql://localhost:3306/" + databaseName; // use correct port here

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(Url, databaseUser, databasePassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databaseLink;
    }

    /**
     * Closes the database connection if it's open
     */
    public void closeConnection() {
        if (databaseLink != null) {
            try {
                if (!databaseLink.isClosed()) {
                    databaseLink.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
