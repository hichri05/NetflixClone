package org.netflix.Services;
import org.mindrot.jbcrypt.BCrypt;


import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;

import java.util.Optional;
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

    /*public Optional<User> register(String nom, String email, String motDePasse) {
        ValidationResult validation = validateRegistration(nom, email, motDePasse);
        if (!validation.isValid()) {
            System.err.println("Erreur validation: " + validation.getMessage());
            return Optional.empty();
        }

        if (userDAO.findByEmail(email) != null) {
            System.err.println("Email déjà utilisé: " + email);
            return Optional.empty();
        }

        // 3. Hacher le mot de passe (NE JAMAIS stocker en clair)
        String hashedPassword = BCrypt.hashpw(motDePasse, BCrypt.gensalt(12));

        // 4. Créer l'utilisateur
        User newUser = new User(nom, email, hashedPassword);

        // 5. Le premier utilisateur inscrit devient automatiquement ADMIN
        /*if (isFirstUser()) {
            newUser.setRole("ADMIN");
            System.out.println("Premier utilisateur créé avec rôle ADMIN");
        }

        // 6. Sauvegarder en base de données
        boolean saved = userDAO.insert(newUser);

        if (saved) {
            return Optional.of(newUser);
        } else {
            return Optional.empty();
        }
    }*/


    public static Boolean login(String email, String password) {
        String hashedPassword = UserDAO.getHashedPass(email);

        //Vérifier que le compte est actif
        /*if (!user.isActif()) {
            System.err.println("Compte désactivé: " + email);
            return Optional.empty();
        }*/

        return hashedPassword != null && BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * Validation complète des données d'inscription
     */
    private ValidationResult validateRegistration(String nom, String email, String motDePasse) {
        // Validation du nom
        if (nom == null || nom.trim().isEmpty()) {
            return ValidationResult.invalid("Le nom ne peut pas être vide");
        }

        if (nom.length() < 2) {
            return ValidationResult.invalid("Le nom doit contenir au moins 2 caractères");
        }

        if (nom.length() > 100) {
            return ValidationResult.invalid("Le nom ne peut pas dépasser 100 caractères");
        }

        // Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("L'email ne peut pas être vide");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.invalid("Format d'email invalide");
        }

        if (email.length() > 255) {
            return ValidationResult.invalid("L'email ne peut pas dépasser 255 caractères");
        }

        // Validation du mot de passe
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

    /**
     * Vérifie si c'est le premier utilisateur (aucun compte existant)
     */
    /*private boolean isFirstUser() {
        // Pour simplifier, on peut compter les utilisateurs
        // Implémentation simplifiée - à améliorer selon besoins
        try {
            return !userDAO.emailExists("admin@jstream.com") &&
                    !userDAO.emailExists("user@jstream.com");
        } catch (Exception e) {
            return true; // En cas d'erreur, on considère que c'est le premier
        }
    }*/

    /**
     * Change le mot de passe d'un utilisateur
     */
    /*public boolean changePassword(User user, String ancienMotDePasse, String nouveauMotDePasse) {
        // 1. Vérifier l'ancien mot de passe
        if (!BCrypt.checkpw(ancienMotDePasse, user.getMotDePasseHash())) {
            System.err.println("Ancien mot de passe incorrect");
            return false;
        }

        // 2. Valider le nouveau mot de passe
        ValidationResult validation = validatePassword(nouveauMotDePasse);
        if (!validation.isValid()) {
            System.err.println("Nouveau mot de passe invalide: " + validation.getMessage());
            return false;
        }

        // 3. Hacher le nouveau mot de passe
        String newHashedPassword = BCrypt.hashpw(nouveauMotDePasse, BCrypt.gensalt(12));

        // 4. Mettre à jour en base
        String sql = "UPDATE utilisateurs SET mot_de_passe_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newHashedPassword);
            pstmt.setInt(2, user.getId());

            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                user.setMotDePasseHash(newHashedPassword);
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validation spécifique du mot de passe
     */
    /*private ValidationResult validatePassword(String motDePasse) {
        if (motDePasse == null || motDePasse.isEmpty()) {
            return ValidationResult.invalid("Le mot de passe ne peut pas être vide");
        }

        if (motDePasse.length() < 8) {
            return ValidationResult.invalid("Le mot de passe doit contenir au moins 8 caractères");
        }

        if (!PASSWORD_PATTERN.matcher(motDePasse).matches()) {
            return ValidationResult.invalid("Le mot de passe ne respecte pas les règles de sécurité");
        }

        return ValidationResult.valid();
    }*/

}