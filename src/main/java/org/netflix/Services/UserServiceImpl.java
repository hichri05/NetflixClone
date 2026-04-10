/*package org.netflix.Services;

import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UserServiceImpl implements IUserService {

    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String email, String password) {
        // Validation basique
        if (email == null || email.isBlank() || password == null || password.isBlank())
            return null;

        User user = userDAO.findByEmail(email);
        if (user == null) return null;

        // Vérification BCrypt
        if (!checkPassword(password, user.getPassword())) return null;

        return user;
    }

    @Override
    public boolean register(String username, String email, String password) {
        if (username == null || username.isBlank()) return false;
        if (email == null || !email.contains("@")) return false;
        if (password == null || password.length() < 6) return false;
        if (emailExists(email)) return false;

        String hashed = hashPassword(password);
        User newUser = new User(username, email, hashed);
        newUser.setRole("USER");
        return userDAO.insert(newUser);
    }

    @Override
    public boolean emailExists(String email) {
        return userDAO.findByEmail(email) != null;
    }

    @Override
    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null) return false;
        return userDAO.update(user);
    }

    @Override
    public boolean deleteUser(int id) {
        return userDAO.delete(id);
    }

    @Override
    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    @Override
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    @Override
    public int countNewUsersPerDay() {
        return userDAO.countRegistrationsToday();
    }
}*/