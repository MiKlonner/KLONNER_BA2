package org.example;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import static org.example.User.dateFormatter;
import static org.example.util.JPA.getEntityManager;

public class UserDAO implements UserDAOInterface {
    @Override
    public boolean save(User user) throws IllegalArgumentException {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            // Prüfen auf Duplikate (Vorname + Nachname + E-Mail)
            List<User> existingUsers = em.createQuery(
                            "SELECT u FROM users u WHERE u.firstName = :firstName AND u.surName = :surName AND u.email = :email",
                            User.class)
                    .setParameter("firstName", user.getFirstName())
                    .setParameter("surName", user.getSurName())
                    .setParameter("email", user.getEmail())
                    .getResultList();

            if (!existingUsers.isEmpty()) {
                System.err.println("Ein Benutzer mit denselben Angaben existiert bereits.");
                return false;
            }

            // Passwortvalidierung
            if (!isPasswordValid(user.getPassword())) {
                System.err.println("Ungültiges Passwort: " + user.getPassword());
                return false;
            }

            // Benutzer speichern
            tx.begin();
            em.persist(user);
            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Fehler beim Speichern des Benutzers: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Validiert das Passwort anhand der vorgegebenen Regeln.
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 10) {
            return false;
        }

        if (password.equalsIgnoreCase("Passwort1!") ||
                password.equalsIgnoreCase("Hallo0000!") ||
                password.equalsIgnoreCase("meinPasswort1!") ||
                password.equalsIgnoreCase("Passwort2!") ||
                password.equalsIgnoreCase("Passwort3!")) {
            return false;
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[^a-zA-Z0-9].*");

        return hasUppercase && hasDigit && hasSpecialChar;
    }



    @Override
    public User updateUserByID(long id, String attribute, String value) throws IllegalArgumentException {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            User user = em.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("Benutzer mit ID " + id + " existiert nicht.");
            }

            tx.begin();

            switch (attribute.toLowerCase()) {
                case "vorname":
                    user.setFirstName(value);
                    break;

                case "nachname":
                    user.setSurName(value);
                    break;

                case "geburtsdatum":
                    try {
                        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                        user.setDateOfBirth(df.parse(value));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Ungültiges Datumsformat. Erwartet: dd.MM.yyyy");
                    }
                    break;

                case "email":
                    user.setEmail(value);
                    break;

                case "berechtigung":
                    Role role = Role.findByTitle(value);
                    if (role == null) {
                        throw new IllegalArgumentException("Ungültige Berechtigungsstufe: " + value);
                    }
                    user.setRole(role);
                    break;

                case "passwort":
                    if (!isPasswordValid(value)) {
                        throw new IllegalArgumentException("Ungültiges Passwort");
                    }
                    user.setPassword(value);
                    break;

                default:
                    throw new IllegalArgumentException("Unbekanntes Attribut: " + attribute);
            }

            em.merge(user);
            tx.commit();
            return user;

        } catch (IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Fehler beim Aktualisieren des Benutzers: " + e.getMessage());
        } finally {
            em.close();
        }
    }


    @Override
    public boolean deleteUserbyID(int id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            User user = em.find(User.class, (long) id); // cast to Long, da ID Long ist
            if (user == null) {
                return false;
            }

            tx.begin();
            em.remove(user);
            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Fehler beim Löschen des Benutzers mit ID " + id + ": " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }


    @Override
    public List<String> uploadCSV(File file) throws RuntimeException {
        List<String> resultLog = new ArrayList<>();
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                resultLog.add("Fehler: CSV-Datei ist leer.");
                return resultLog;
            }

            String[] headers = headerLine.split(";");
            if (headers.length != 6 ||
                    !headers[0].equalsIgnoreCase("Vorname") ||
                    !headers[1].equalsIgnoreCase("Nachname") ||
                    !headers[2].equalsIgnoreCase("Geburtsdatum") ||
                    !headers[3].equalsIgnoreCase("Email") ||
                    !headers[4].equalsIgnoreCase("Berechtigung") ||
                    !headers[5].equalsIgnoreCase("Passwort")) {
                resultLog.add("Fehler: Header ist unvollständig oder inkorrekt.");
                return resultLog;
            }

            List<User> usersToImport = new ArrayList<>();
            String line;
            int lineNumber = 2; // da Header = 1

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields.length != 6) {
                    resultLog.add("Fehler in Zeile " + lineNumber + ": Ungültige Spaltenanzahl.");
                    return resultLog;
                }

                // Leere Felder prüfen
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].trim().isEmpty()) {
                        resultLog.add("Fehler in Zeile " + lineNumber + ": Leeres Attribut - " + headers[i]);
                        return resultLog;
                    }
                }

                try {
                    User user = new User(
                            fields[0].trim(), // Vorname
                            fields[1].trim(), // Nachname
                            fields[2].trim(), // Geburtsdatum
                            fields[3].trim(), // Email
                            fields[5].trim(), // Passwort
                            fields[4].trim()  // Berechtigung
                    );

                    if (!isPasswordValid(user.getPassword())) {
                        resultLog.add("Fehler in Zeile " + lineNumber + ": Ungültiges Passwort");
                        return resultLog;
                    }

                    usersToImport.add(user);

                } catch (IllegalArgumentException e) {
                    resultLog.add("Fehler in Zeile " + lineNumber + ": " + e.getMessage());
                    return resultLog;
                }

                lineNumber++;
            }

            // Wenn alle Zeilen gültig -> speichern
            tx.begin();
            for (User user : usersToImport) {
                em.persist(user);
            }
            tx.commit();
            resultLog.add("Der Import war erfolgreich und die Daten stehen zur weiteren Verarbeitung in der Datenbank bereit.");
            return resultLog;

        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der Datei: " + e.getMessage());
        } finally {
            em.close();
        }
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