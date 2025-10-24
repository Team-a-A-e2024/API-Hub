package dat.security.daos;

import dat.security.dtos.UserDTO;
import dat.security.entities.Role;
import dat.security.entities.User;
import dat.security.exceptions.ApiException;
import dat.security.exceptions.ValidationException;
import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

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
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            User user;
            try {
                user = query.getSingleResult();
            } catch (NoResultException e) {
                throw new EntityNotFoundException("No user found with username: " + username);
            }
            user.getRoles().size();
            if (!user.verifyPassword(password)) {
                throw new ValidationException("Wrong password");
            }
            return new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getRoles().stream()
                            .map(Role::getRoleName)
                            .collect(Collectors.toSet())
            );
        }
    }

    @Override
    public User createUser(String username, String password) {
        try (EntityManager em = getEntityManager()) {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            User existingUser = null;
            try {
                existingUser = query.getSingleResult();
            } catch (NoResultException e) {
            }
            if (existingUser != null)
                throw new EntityExistsException("User with username: " + username + " already exists");
            User userEntity = new User(username, password);
            em.getTransaction().begin();
            Role userRole = em.createQuery("SELECT r FROM Role r WHERE r.name = :role", Role.class)
                    .setParameter("role", "user")
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (userRole == null) {
                userRole = new Role("user");
                em.persist(userRole);
            }
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public User addRole(User user, String newRole) {
        try (EntityManager em = getEntityManager()) {
            if (user == null)
                throw new EntityNotFoundException("User cannot be null");
            User managedUser = em.merge(user);
            em.getTransaction().begin();
            Role role = em.createQuery("SELECT r FROM Role r WHERE r.name = :role", Role.class)
                    .setParameter("role", newRole)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (role == null) {
                role = new Role(newRole);
                em.persist(role);
            }
            managedUser.addRole(role);
            em.getTransaction().commit();
            return managedUser;
        }
    }

    @Override
    public User editUser(User user) {
        try (EntityManager em = getEntityManager()) {
            User checkedUser = em.find(User.class, user.getId());
            if (checkedUser == null) {
                throw new EntityNotFoundException("No user found with id: " + user.getId());
            }
            em.getTransaction().begin();
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                if (!user.getPassword().equals(checkedUser.getPassword())) {
                    String hashedPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                    checkedUser.setPassword(hashedPw);
                }
            }
            em.merge(checkedUser);
            em.getTransaction().commit();
            return checkedUser;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try (EntityManager em = getEntityManager()) {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            User user;
            try {
                user = query.getSingleResult();
            } catch (NoResultException e) {
                throw new EntityNotFoundException("No user found with username: " + username);
            }
            user.getRoles().size();
            return user;
        }
    }

    @Override
    public User getUserById(Integer id) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, id);
            if (user == null)
                throw new EntityNotFoundException("No user found with id: " + id);
            user.getRoles().size(); // Force lazy loading
            return user;
        }
    }

    @Override
    public void deleteUser(Integer id) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, id);
            if (user == null) {
                throw new EntityNotFoundException("No user found with id: " + id);
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