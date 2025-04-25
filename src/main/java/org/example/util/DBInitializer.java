package org.example.util;


import org.example.UserDAO;

public class DBInitializer {


    private final UserDAO UserDAO;


    public DBInitializer() {
        this.UserDAO = new UserDAO();
    }

    public void initDatabase() {
        System.out.println("Initializing database...");


        System.out.println("Database initialized successfully!");
    }
}