// package org.netflix.Services;
package org.netflix.Services;

import org.netflix.Models.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    User login(String email, String password);
    boolean register(String username, String email, String password);
    boolean emailExists(String email);
    Optional<User> getUserById(int id);
    List<User> getAllUsers();
    boolean updateUser(User user);
    boolean deleteUser(int id);
    String hashPassword(String rawPassword);
    boolean checkPassword(String rawPassword, String hashedPassword);
    Map<String, Long> countNewUsersPerDay();
}