package dat.security.daos;
import dat.config.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class SecurityDAOTest {

    private EntityManagerFactory emf;
    private SecurityDAO dao;

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE")
                    .executeUpdate();
            em.getTransaction().commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    void setupOnce() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        dao = new SecurityDAO(emf);
    }

    @AfterAll
    void teardown() {
        if (emf != null && emf.isOpen()) emf.close();
    }

    @Test
    void getVerifiedUser() {
    }

    @Test
    void createUser() {
    }

    @Test
    void addRole() {
    }

    @Test
    void editUser() {
    }

    @Test
    void getUserByUsername() {
    }

    @Test
    void deleteUser() {
    }
}