package org.example;

import java.util.ArrayList;
import java.util.Arrays;
public class PasswordValidator {

    public static final int minLength = 10;
    public static final ArrayList<String> exclude = new ArrayList<>(Arrays.asList("Passwort1!","Hallo0000","meinPasswort1!","Passwort2","Passwort3!"));



    public static boolean isValid(String password){
        boolean valid = false;
        boolean hasUpperCase = false;
        boolean hasSpecialCharakter = false;
        boolean hasDigit = false;
        char character=' ';
        if((password.length() < minLength)){
            return false;
        }
        if((exclude.contains(password))){
            return false;
        }
        for(int i=0; i< password.length();i++){
            character = password.charAt(i);
            if(Character.isUpperCase(character)){
                hasUpperCase = true;
            }
            if(Character.isDigit(character)){
                hasDigit = true;
            }
            if(!Character.isDigit(character) && !Character.isLetter(character) && !Character.isSpace(character)){
                hasSpecialCharakter = true;
            }
        }

        if(hasUpperCase && hasDigit && hasSpecialCharakter){
            valid = true;
        }
        return valid;
    }

}
