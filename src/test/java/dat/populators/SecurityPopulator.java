package dat.populators;

import dat.security.daos.SecurityDAO;
import dat.security.entities.User;

import java.util.List;

public class SecurityPopulator {
    public static List<User> populateUsers(SecurityDAO securityDAO) {
        User u1 = securityDAO.createUser("A", "A1");
        User u2 = securityDAO.createUser("B", "B1");
        User u3 = securityDAO.createUser("C", "C1");

        securityDAO.addRole(u1, "user");
        securityDAO.addRole(u2, "admin");
        securityDAO.addRole(u3, "guest");

        return List.of(u1, u2, u3);
    }
}
