package org.netflix.Utils;

import org.netflix.Models.User;

public class Session {
    private static User user;
    public static User getUser() {
        return user;
    }
    public static void setUser(User u) {
        user = u;
    }
    public static void logout() {
        user = null;
    }
}
