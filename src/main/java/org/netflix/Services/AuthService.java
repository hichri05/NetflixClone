package org.netflix.Services;

import org.mindrot.jbcrypt.BCrypt;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;

import java.util.regex.Pattern;

public class AuthService {

    private final UserDAO userDAO;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public static Boolean register(String nom, String email, String motDePasse) {
        if (!validateRegistration(nom, email, motDePasse).isValid() || UserDAO.findByEmail(email) != null) {
            return false;
        }
        String hashedPassword = BCrypt.hashpw(motDePasse, BCrypt.gensalt(12));
        User newUser = new User(nom, email, hashedPassword);
        return UserDAO.AddUser(newUser);
    }

    public static Boolean login(String email, String password) {
        String hashedPassword = UserDAO.getHashedPass(email);
        return hashedPassword != null && BCrypt.checkpw(password, hashedPassword);
    }

    private static ValidationResult validateRegistration(String nom, String email, String motDePasse) {
        if (nom == null || nom.trim().isEmpty()) {
            return ValidationResult.invalid("Le nom ne peut pas être vide");
        }
        if (nom.length() < 2) {
            return ValidationResult.invalid("Le nom doit contenir au moins 2 caractères");
        }
        if (nom.length() > 100) {
            return ValidationResult.invalid("Le nom ne peut pas dépasser 100 caractères");
        }
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("L'email ne peut pas être vide");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.invalid("Format d'email invalide");
        }
        if (email.length() > 255) {
            return ValidationResult.invalid("L'email ne peut pas dépasser 255 caractères");
        }
        if (motDePasse == null || motDePasse.isEmpty()) {
            return ValidationResult.invalid("Le mot de passe ne peut pas être vide");
        }
        if (motDePasse.length() < 8) {
            return ValidationResult.invalid("Le mot de passe doit contenir au moins 8 caractères");
        }
        if (!PASSWORD_PATTERN.matcher(motDePasse).matches()) {
            return ValidationResult.invalid(
                    "Le mot de passe doit contenir au moins:\n" +
                            "- 1 chiffre\n" +
                            "- 1 lettre minuscule\n" +
                            "- 1 lettre majuscule\n" +
                            "- 1 caractère spécial (@#$%^&+=)\n" +
                            "- Pas d'espace"
            );
        }
        return ValidationResult.valid();
    }
}