package com.library.library_system;

import org.hibernate.Session;
import com.library.library_system.util.SessionFactoryProvider;

public class App {
    public static void main(String[] args) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            System.out.println("Successfully connected to the database!");
        } catch (Exception e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }
}