package org.example;

import java.io.File;
import java.util.List;


public interface UserDAOInterface {


    boolean save(User user) throws RuntimeException;


    User updateUserByID(long id, String attribute, String value) throws RuntimeException;


    boolean deleteUserbyID(int id);



    List<String> findByFirstName(String name);



    List<String> uploadCSV(File file) throws RuntimeException;
}
