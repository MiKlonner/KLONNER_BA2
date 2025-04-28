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
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            if (ifUserExists(user)) {
                return false;
            }

            if (!PasswordValidator.isValid(user.getPassword())) {
                return false;
            }
            transaction.begin();
            entityManager.persist(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            entityManager.close();
        }
    }
    private static boolean ifUserExists(User user) {
        EntityManager em = getEntityManager();

        List existingUsers = em.createQuery("SELECT u FROM users u WHERE u.firstName = :firstName AND u.surName = :lastName AND u.email = :email")
                .setParameter("firstName", user.getFirstName())
                .setParameter("lastName", user.getSurName())
                .setParameter("email", user.getEmail())
                .getResultList();
        return !existingUsers.isEmpty(); //true if User does exist
    }

    public User updateUserByID(long id, String attribute, String value) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        User user = em.find(User.class, id);
        // Check if user exists
        if (user == null) {
            throw new IllegalArgumentException("User mit ID " + id + " existiert nicht");
        }
        try {
            switch (attribute) {
                case "Vorname":
                    user.setFirstName(value);
                    break;
                case "Nachname":
                    user.setSurName(value);
                    break;
                case "email":
                    user.setEmail(value);
                    break;
                case "Geburtsdatum":
                    user.setDateOfBirth(dateFormatter.parse(value));
                    break;
                case "Berechtigung":
                    Role foundRole = null;
                    for (Role role : Role.values()) {
                        if (role.getTitle().equalsIgnoreCase(value)) {
                            foundRole = role;
                            break;
                        }
                    }
                    if (foundRole != null) {
                        user.setRole(foundRole);
                    } else {
                        throw new IllegalArgumentException("Ungültige Berechtigungsstufe: " + value);
                    }
                    break;
                case "Passwort":
                    user.setPassword(value);
                    break;
                default:
                    throw new IllegalArgumentException("Das angegebene Attribut existiert nicht. " +
                            "Folgende Attribute sind möglich: Vorname, Nachname, email, Geburtsdatum, Berechtigung.");
            }
            transaction.begin();
            User newUser = em.merge(user);
            transaction.commit();
            return newUser;
        } catch (IllegalArgumentException e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteUserbyID(int id) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            User user = em.find(User.class, id);
            transaction.begin();
            em.remove(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();

            return false;
        }
    }

    public List<String> uploadCSV(File file) throws RuntimeException {
        List<User> uploadedUsers = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath())))) {
            String line;
            String header;
            boolean correctHeader = false;
            int countLine=0;


            //check if header is correct
            header = reader.readLine();
            countLine++;
            String[] headerDetails = header.split(",");
            if(headerDetails[0].equalsIgnoreCase("Vorname")  && headerDetails[1].equalsIgnoreCase("Nachname")  && headerDetails[2].equalsIgnoreCase("Geburtsdatum")  && headerDetails[3].equalsIgnoreCase("Email")  && headerDetails[4].equalsIgnoreCase("Berechtigung") && headerDetails[5].equalsIgnoreCase("Passwort") ) {
                correctHeader = true;
            }


            //read all lines
            if(correctHeader){
                while ((line = reader.readLine()) != null) {
                    countLine++;
                    String[] details = line.split(",");
                    User user = new User();

                    if(details[0]!=null){
                        user.setFirstName(details[0]);
                    }
                    else{
                        errorMessages.add("In Zeile" + countLine + "fehlt der Vorname");
                    }
                    if(details[1]!=null) {
                        user.setSurName(details[1]);
                    }
                    else{
                        errorMessages.add("In Zeile" + countLine + "fehlt der Nachname");
                    }
                    if(details[2]!=null) {
                        try {
                            user.setDateOfBirth(dateFormatter.parse(details[2]));
                        } catch (ParseException e) {
                            errorMessages.add("Bitte geben Sie das Geburtsdatum in folgendem Format an:" + details[2].toString());
                        }
                    }
                    else{
                        errorMessages.add("In Zeile" + countLine + "fehlt das Geburtsdatum");
                    }

                    if(details[3]!=null) {
                        user.setEmail(details[3]);
                    }
                    else{
                        errorMessages.add("In Zeile" + countLine + "fehlt die Mailadresse");
                    }

                    if(details[4]!=null) {
                        Role foundRole = Role.findByTitle(details[4]);
                        if (foundRole == null) {
                            errorMessages.add("Bitte geben Sie eine gültige Berechtigungsstufe an");
                        } else {
                            user.setRole(Role.findByTitle(details[4]));
                        }
                    }
                    else{
                        errorMessages.add("In Zeile" + countLine + "fehlt die Berechtigungsstufe");
                    }

                    if(details[5]!=null){
                        if (PasswordValidator.isValid(details[5])) {
                            user.setPassword(details[5]);
                        }
                        else {
                            errorMessages.add("Bitte geben Sie ein gültiges Passwort ein");
                        }
                    }
                    else{
                        errorMessages.add("In Zeile" + countLine + "fehlt das Passwort");
                    }
                    uploadedUsers.add(user);
                }
                if (errorMessages.isEmpty()) {
                    for (User user : uploadedUsers){
                        save(user);
                    }
                    errorMessages.add("Der Import war erfolgreich und die Daten stehen zur weiteren Verarbeitung in der Datenbank bereit.");
                }
                else {
                    errorMessages.add("Upload war nicht erfolgreich");
                }
            }
            else {
                errorMessages.add("Bitte geben Sie die Parameter in folgender Reiohenfolge an: Vorname,Nachname,Geburtsdatum,Email,Berechtigung,Passwort");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return errorMessages;
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