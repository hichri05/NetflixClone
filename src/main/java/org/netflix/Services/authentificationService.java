// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {

    private final UserDAO userDAO;

    // Patterns pour validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Inscription d'un nouvel utilisateur
     * @param username Nom d'utilisateur
     * @param email Adresse email
     * @param motDePasse Mot de passe en clair
     * @return Optional contenant l'utilisateur créé ou vide si erreur
     */
    public Optional<User> register(String username, String email, String motDePasse) {
        // 1. Validation des entrées
        ValidationResult validation = validateRegistration(username, email, motDePasse);
        if (!validation.isValid()) {
            System.err.println("Erreur validation: " + validation.getMessage());
            return Optional.empty();
        }

        // 2. Vérifier si l'email existe déjà
        if (userDAO.emailExists(email)) {
            System.err.println("Email déjà utilisé: " + email);
            return Optional.empty();
        }

        // 3. Vérifier si le username existe déjà
        if (userDAO.usernameExists(username)) {
            System.err.println("Nom d'utilisateur déjà utilisé: " + username);
            return Optional.empty();
        }

        // 4. Hacher le mot de passe (NE JAMAIS stocker en clair)
        String hashedPassword = BCrypt.hashpw(motDePasse, BCrypt.gensalt(12));

        // 5. Créer l'utilisateur (id = 0 car sera généré par la BDD)
        User newUser = new User(0, username, email);

        // 6. Sauvegarder en base de données
        Optional<User> savedUser = userDAO.insert(newUser, hashedPassword);

        if (savedUser.isPresent()) {
            System.out.println("Inscription réussie: " + savedUser.get().getUsername());
            return savedUser;
        } else {
            System.err.println("Échec de l'inscription en base de données");
            return Optional.empty();
        }
    }

    /**
     * Connexion d'un utilisateur
     * @param email Email
     * @param motDePasse Mot de passe en clair
     * @return Optional contenant l'utilisateur connecté ou vide si échec
     */
    public Optional<User> login(String email, String motDePasse) {
        // 1. Validation basique
        if (email == null || email.trim().isEmpty() ||
                motDePasse == null || motDePasse.isEmpty()) {
            System.err.println("Email ou mot de passe vide");
            return Optional.empty();
        }

        // 2. Rechercher l'utilisateur par email
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.err.println("Utilisateur non trouvé: " + email);
            return Optional.empty();
        }

        User user = userOpt.get();

        // 3. Récupérer le hash du mot de passe depuis la BDD
        Optional<String> passwordHashOpt = userDAO.getPasswordHash(user.getId());

        if (passwordHashOpt.isEmpty()) {
            System.err.println("Hash du mot de passe introuvable");
            return Optional.empty();
        }

        // 4. Vérifier le mot de passe (comparaison avec le hash)
        if (!BCrypt.checkpw(motDePasse, passwordHashOpt.get())) {
            System.err.println("Mot de passe incorrect pour: " + email);
            return Optional.empty();
        }

        // 5. Connexion réussie
        System.out.println("Connexion réussie: " + user.getUsername() + " (" + email + ")");
        return Optional.of(user);
    }

    /**
     * Validation complète des données d'inscription
     */
    private ValidationResult validateRegistration(String username, String email, String motDePasse) {
        // Validation du username
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.invalid("Le nom d'utilisateur ne peut pas être vide");
        }

        if (username.length() < 3) {
            return ValidationResult.invalid("Le nom d'utilisateur doit contenir au moins 3 caractères");
        }

        if (username.length() > 50) {
            return ValidationResult.invalid("Le nom d'utilisateur ne peut pas dépasser 50 caractères");
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return ValidationResult.invalid("Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores");
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
     * Change le mot de passe d'un utilisateur
     * @param user Utilisateur
     * @param ancienMotDePasse Ancien mot de passe en clair
     * @param nouveauMotDePasse Nouveau mot de passe en clair
     * @return true si changement réussi, false sinon
     */
    public boolean changePassword(User user, String ancienMotDePasse, String nouveauMotDePasse) {
        // 1. Vérifier l'ancien mot de passe
        Optional<String> currentHashOpt = userDAO.getPasswordHash(user.getId());

        if (currentHashOpt.isEmpty()) {
            System.err.println("Utilisateur non trouvé");
            return false;
        }

        if (!BCrypt.checkpw(ancienMotDePasse, currentHashOpt.get())) {
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
        boolean updated = userDAO.updatePassword(user.getId(), newHashedPassword);

        if (updated) {
            System.out.println("Mot de passe changé avec succès pour: " + user.getUsername());
        } else {
            System.err.println("Échec du changement de mot de passe");
        }

        return updated;
    }

    /**
     * Validation spécifique du mot de passe
     */
    private ValidationResult validatePassword(String motDePasse) {
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
    }

    /**
     * Vérifie si l'utilisateur est administrateur
     * @param userId ID de l'utilisateur
     * @return true si administrateur, false sinon
     */
    public boolean isAdmin(int userId) {
        return userDAO.isAdmin(userId);
    }

    /**
     * Récupère le rôle de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return "ADMIN" ou "USER"
     */
    public String getUserRole(int userId) {
        return userDAO.getUserRole(userId);
    }

    /**
     * Déconnecte l'utilisateur (logique métier simple)
     * @param user Utilisateur à déconnecter
     */
    public void logout(User user) {
        System.out.println("Déconnexion de: " + user.getUsername());
        // Ici on peut ajouter des logs ou nettoyer des sessions
    }

    /**
     * Classe interne pour les résultats de validation
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}