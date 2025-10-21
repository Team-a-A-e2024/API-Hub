package dat.security.daos;


import dat.security.entities.Role;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Handler;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + username);
            user.getRoles().size();
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return new UserDTO(user.getUsername(), user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }
    }

    @Override
    public User createUser(String username, String password) {
        try (EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, username);
            if (userEntity != null)
                throw new EntityExistsException("User with username: " + username + " already exists");
            userEntity = new User(username, password);
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "user");
            if (userRole == null)
                userRole = new Role("user");
            em.persist(userRole);
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public User addRole(UserDTO userDTO, String newRole) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, userDTO.getUsername());
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + userDTO.getUsername());
            em.getTransaction().begin();
                Role role = em.find(Role.class, newRole);
                if (role == null) {
                    role = new Role(newRole);
                    em.persist(role);
                }
                user.addRole(role);
            em.getTransaction().commit();
            return user;
        }
    }

    public User editUser(UserDTO userDTO) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, userDTO.getUsername());
            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + userDTO.getUsername());
            }
            em.getTransaction().begin();
            user.setPassword(userDTO.getPassword());
            em.merge(user);
            em.getTransaction().commit();
            return user;
        }
    }

    public User getUserByUsername(String username) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + username);
            user.getRoles().size();
            return user;
        }
    }

    public void deleteUser(String username) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + username);
            }
            em.getTransaction().begin();
            for (Role role : user.getRoles()) {
                role.getUsers().remove(user);
            }
            user.getRoles().clear();
            em.remove(user);
            em.getTransaction().commit();
        }
    }







}

