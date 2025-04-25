package org.example;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



public class UserTest {
    private static EntityManagerFactory emf;
    private UserDAO userDAO;

    @BeforeAll
    static void clearDatabase() {
        emf = Persistence.createEntityManagerFactory("h2-persistence-unit");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null){
            emf.close();
        }
    }

    @BeforeEach
    void setup() {
        userDAO = new UserDAO();
    }

    @Test
    @Order(1)
    public void testSaveValidUser() {
        // Create a valid user with all required attributes
        User user = new User(
                "Han", "Solo",
                "01.01.2000", "han.solo@mail.at",
                "DidPWvMK1!", "ADMIN");
        User user1 = new User(
                "Luke", "Skywalker",
                "01.01.2000", "luke@mail.at",
                "DidPWvMK1!", "ADMIN");

        userDAO.save(user);
        userDAO.save(user1);
        boolean result;
        if(userDAO.findByFirstName("Han").isEmpty()){
            result = false;
        } else{
            result = true;
        }

        // Verify the result
        assertTrue(result, "User should be saved successfully");
    }
    @Test
    @Order(2)
    public void testSaveExistingUser() {
        // Create a valid user with all required attributes
        User user = new User(
                "Fritz", "Maier",
                "01.01.2000", "mi.fritz@gmail.com",
                "DidPWvMK1!", "ADMIN");

        User user1 = new User(
                "Fritz", "Maier",
                "01.01.2010", "mi.fritz@gmail.com",
                "DidPWvMK1!", "GUEST");
        boolean result;

        userDAO.save(user);
        userDAO.save(user1);
        List<String> users= userDAO.findByFirstName("Fritz");

        if(users.size() >1){
            result = false;
        } else{
            result = true;
        }
        // Verify the result
        assertTrue(result, "User could not be saved");
    }

    @Test
    @Order(3)
    public void testSaveInvalidPasswordMissingCapital() {
        User user = new User(
                "Franz", "Mueller",
                "01.01.2000", "mi.fritz@gmail.com",
                "hallo123456!", "ADMIN");

        userDAO.save(user);

        boolean result;
        if(userDAO.findByFirstName("Franz").isEmpty()){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "User could not be saved");
    }

    @Test
    @Order(4)
    public void testSaveInvalidPasswordMissingSpecialCharacter() {
        User user = new User(
                "Michael", "Klonner",
                "01.01.2000", "mi.fritz@gmail.com",
                "Hallo1234567", "ADMIN");

        userDAO.save(user);

        boolean result;
        if(userDAO.findByFirstName("Michael").isEmpty()){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "User could not be saved");
    }
    @Test
    @Order(5)
    public void testSaveInvalidPasswordMissingDigit() {
        User user = new User(
                "Max", "Mustermann",
                "01.01.2000", "mi.fritz@gmail.com",
                "Hallooooooooooo!", "ADMIN");

        userDAO.save(user);

        boolean result;
        if(userDAO.findByFirstName("Max").isEmpty()){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "User could not be saved");
    }

    @Test
    @Order(6)
    public void testSaveExcludedPassword() {
        User user = new User(
                "Paul", "Wagner",
                "01.01.2000", "mi.fritz@gmail.com",
                "meinPasswort1!", "ADMIN");

        boolean result;
        if(userDAO.findByFirstName("Paul").isEmpty()){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "User could not be saved");
    }
    @Test
    @Order(7)
    public void testSaveShortPassword() {
        User user = new User(
                "Lea", "Schulz",
                "01.01.2000", "mi.fritz@gmail.com",
                "Hallo1!", "ADMIN");

        boolean result;
        if(userDAO.findByFirstName("Lea").isEmpty()){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "User could not be saved");
    }
    @Test
    @Order(8)
    public void testSaveWrongDateFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("Hans", "Rechtberger", "01-01-2000", "mi.fritz@gmail.com", "Hallo123456!", "ADMIN");
        });
    }

    @Test
    @Order(9)
    public void testSaveWrongRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            new User("Hans", "Rechtberger", "01.01.2000", "mi.fritz@gmail.com", "Hallo123456!", "ROLE");
        });
    }


    @Test
    @Order(10)
    public void testUpdateFirstName() {

        // Update and verify
        User updatedUser = userDAO.updateUserByID(1, "Vorname", "Hans-Peter");
        assertEquals("Hans-Peter", updatedUser.getFirstName(), "First name should be updated");
    }
    @Test
    @Order(11)
    public void testUpdateSurName() {

        // Update and verify
        User updatedUser = userDAO.updateUserByID(1, "Nachname", "Doskozil");
        assertEquals("Doskozil", updatedUser.getSurName(), "Surname should be updated");
    }
    @Test
    @Order(12)
    public void testUpdateDateOfBirth() {

        // Update and verify
        User updatedUser = userDAO.updateUserByID(1, "Geburtsdatum", "02.02.1900");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedDate = formatter.format(updatedUser.getDateOfBirth());

        assertEquals("02.02.1900 00:00:00", formattedDate, "Date of Birth should be updated");
    }
    @Test
    @Order(13)
    public void testUpdateEmail() {

        // Update and verify
        User updatedUser = userDAO.updateUserByID(1, "email", "hp@dosko.at");
        assertEquals("hp@dosko.at", updatedUser.getEmail(), "Email should be updated");
    }
    @Test
    @Order(14)
    public void testUpdateRole() {
        User user = new User(
                "UpdateRole", "User",
                "01.01.2000", "test@example.com",
                "ValidPass1!", "ADMIN"
        );
        userDAO.save(user);
        // Update and verify

        User updatedUser = userDAO.updateUserByID(user.getId(), "Berechtigung", "USER");
        assertEquals(Role.USER, updatedUser.getRole(), "Role should be updated");
    }

    @Test
    @Order(15)
    public void testUpdatePassword() {

        // Update and verify
        User updatedUser = userDAO.updateUserByID(1, "Passwort", "DidPWvHPD1!");
        assertEquals("DidPWvHPD1!", updatedUser.getPassword(), "Password should be updated");
    }

    @Test
    @Order(16)
    public void testUpdateNonExistingUser() {
        assertThrows(IllegalArgumentException.class, () -> {
        userDAO.updateUserByID(100, "Vorname","Hans-Peter");
        });
    }

    @Test
    @Order(17)
    public void testUpdateWrongRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.updateUserByID(1, "Berechtigung","TEST");
        }, "Ungültige Berechtigungsstufe: TEST");
    }
    @Test
    @Order(18)
    public void testDeleteUser() {
        boolean result = userDAO.deleteUserbyID(3);

        // Verify the result
        assertTrue(result, "User should be deleted successfully"); }

    @Test
    @Order(19)
    public void testDeleteNonEsixtingUser() {
        boolean result = userDAO.deleteUserbyID(100);
        // Verify the result
        assertFalse(result, "User should be deleted successfully"); }

    @Test
    @Order(20)
    public void testValidUpload() {
        URL resource = getClass().getClassLoader().getResource("CSV/AccUser.csv");
        assertNotNull(resource, "CSV file not found in resources");
        File testCSV = new File(resource.getFile());
        List<String> importLog = userDAO.uploadCSV(testCSV);
        importLog.forEach(System.out::println);
        boolean result;

        if(userDAO.findByFirstName("Upload").isEmpty() || (importLog.isEmpty())){
            result = false;
        } else{
            result = true;
        }
    assertTrue(result, "Upload war Erfolgreich");


    }

    @Test
    @Order(21)
    public void testUploadMissingAttributeHeader() {
        URL resource = getClass().getClassLoader().getResource("CSV/MissingAttributeHeader.csv");
        assertNotNull(resource, "CSV file not found in resources");
        File testCSV = new File(resource.getFile());
        List<String> importLog = userDAO.uploadCSV(testCSV);
        importLog.forEach(System.out::println);
        boolean result;

        if(userDAO.findByFirstName("MissingAttribute").isEmpty() && !(importLog.isEmpty())){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "Upload war nicht erfolgreich. Es fehlt ein Attribut im Header");
        }

    @Test
    @Order(22)
    public void testUploadIncompleteUser() {
        URL resource = getClass().getClassLoader().getResource("CSV/IncompleteUser.csv");
        assertNotNull(resource, "CSV file not found in resources");
        File testCSV = new File(resource.getFile());
        List<String> importLog = userDAO.uploadCSV(testCSV);
        importLog.forEach(System.out::println);
        boolean result;

        if(userDAO.findByFirstName("IncompleteUser").isEmpty() && !(importLog.isEmpty())){
            result = true;
        } else{
            result = false;
        }
        assertTrue(result, "Upload war nicht erfolgreich. Es fehlt ein Attribut");
    }
 }



