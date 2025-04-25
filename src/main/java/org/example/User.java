package org.example;

import jakarta.persistence.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Entity (name= "users")
@Table(name = "users")
public class User {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String firstName;
    protected String surName;
    protected Date dateOfBirth;
    protected String email;


    @Enumerated(EnumType.STRING)
    protected Role role;
    protected String password;

    static protected SimpleDateFormat dateFormatter;



    public User() {

    }


    public User (String firstName, String surName, String dateOfBirth, String email, String password, String role) throws RuntimeException {
        this.dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            this.dateOfBirth = dateFormatter.parse(dateOfBirth);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Bitte geben Sie das Geburtsdatum in folgendem FOrmat an:" + dateOfBirth.toString());
        }
        this.firstName = firstName;
        this.surName = surName;
        this.email = email;

        this.password = password;

        Role foundRole = Role.findByTitle(role);
        if (foundRole == null) {
            throw new IllegalArgumentException("Bitte geben Sie eine gültige Berechtigungsstufe an");
        }
        else{
            this.role = (Role.findByTitle(role));
        }


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", surName='" + surName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", password='" + password + '\'' +
                '}';
    }
}