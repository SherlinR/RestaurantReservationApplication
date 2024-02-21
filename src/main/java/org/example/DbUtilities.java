package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;

// Manages Connection to DB
public class DbUtilities {

    //
    private static final String dbConnectionUrl = "jdbc:sqlserver://LAPTOP-L97R2MHQ:1433;databaseName=RestaurantDB;user=test;password=test;";

    // Persist the Customers in processed Queue by Inserting in DB
    public static void insertProcessedCustomers(Queue<Customer> queueCopy) {
        String insertSql = "INSERT INTO Customers (Id, FirstName, LastName, Email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbConnectionUrl)) {
            while (!queueCopy.isEmpty()) {
                // Retrieve the Customer details from Processed Queue and Insert in Customers DB table.
                Customer customer = queueCopy.poll();

                try (PreparedStatement preparedStatement = conn.prepareStatement(insertSql)) {
                    preparedStatement.setInt(1, customer.id);
                    preparedStatement.setString(2, customer.firstName);
                    preparedStatement.setString(3, customer.lastName);
                    preparedStatement.setString(4, customer.email);

                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Error inserting customer in DB: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection failure: " + e.getMessage());
        }
    }
}
