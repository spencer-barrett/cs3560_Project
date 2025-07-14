package com.library.library_system.util;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/*
 * Utility class for the Session Factory
 * Handles the creation and retrieval of the session factory
 */
public class SessionFactoryProvider {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Exception ex) {
            System.err.println("SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}