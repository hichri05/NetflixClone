package org.netflix.Services;

public class ValidationService {

    public static ValidationResult validate(String email, String userName, String password, String passwordConfirm) {

        if (email.isEmpty() || userName.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            return ValidationResult.invalid("Please fill in all fields.");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ValidationResult.invalid("Please enter a valid email address.");
        }

        if (userName.length() < 2) {
            return ValidationResult.invalid("Username must be at least 2 characters.");
        }

        if (password.length() < 8) {
            return ValidationResult.invalid("Password must be at least 8 characters.");
        }

        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            return ValidationResult.invalid("Password must contain: uppercase, lowercase, digit, and special character (@#$%^&+=).");
        }

        if (!password.equals(passwordConfirm)) {
            return ValidationResult.invalid("Passwords do not match.");
        }

        return ValidationResult.valid();
    }
}
