package dat.security.daos;

import dat.security.dtos.UserDTO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;

public interface ISecurityDAO {
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    User createUser(String username, String password);
    User addRole(UserDTO user, String newRole);
    User getUserByUsername(String username);
    User editUser(UserDTO userDTO);
    void deleteUser(String username);
}
