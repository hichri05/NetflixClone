// package org.netflix.Services;
package org.netflix.Services;

import org.netflix.Models.User;
import java.util.List;

public interface IUserService {
    User login(String email, String password);
    boolean register(String username, String email, String password);
    boolean emailExists(String email);
    User getUserById(int id);
    List<User> getAllUsers();
    boolean updateUser(User user);
    boolean deleteUser(int id);
    String hashPassword(String rawPassword);
    boolean checkPassword(String rawPassword, String hashedPassword);
    int countNewUsersPerDay(); // Dashboard analytique
}