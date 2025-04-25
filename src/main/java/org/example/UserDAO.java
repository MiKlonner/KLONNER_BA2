package org.example;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import static org.example.User.dateFormatter;
import static org.example.util.JPA.getEntityManager;

public class UserDAO implements UserDAOInterface {
    public boolean save(User user) throws IllegalArgumentException {


        return null;
    }


    public User updateUserByID(long id, String attribute, String value) {

        return null;
    }

    public boolean deleteUserbyID(int id) {

        return null;
    }

    public List<String> uploadCSV(File file) throws RuntimeException {

        return null;
    }
    public List<String> findByFirstName(String name){
        EntityManager em = getEntityManager();
        List<String> firstNames = em.createQuery(
                        "SELECT u.firstName FROM users u WHERE u.firstName = :name", String.class)
                .setParameter("name", name)
                .getResultList();
        return firstNames;
    }
}