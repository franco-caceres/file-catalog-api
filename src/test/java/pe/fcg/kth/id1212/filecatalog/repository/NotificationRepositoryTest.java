package pe.fcg.kth.id1212.filecatalog.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import pe.fcg.kth.id1212.filecatalog.entity.NotificationEntity;
import pe.fcg.kth.id1212.filecatalog.entity.UserEntity;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    private UserEntity otherUser;

    @Before
    public void setUp() {
        user = new UserEntity();
        user.setName("franco");
        user.setPassword("doesnotmatter");
        userRepository.save(user);

        otherUser = new UserEntity();
        otherUser.setName("other");
        otherUser.setPassword("doesnotmatter");
        userRepository.save(otherUser);
    }

    @Test
    public void onlyRetrieveNonFetched() {
        Calendar now = Calendar.getInstance();
        NotificationEntity a = new NotificationEntity();
        a.setCreatedOn(now.getTime());
        a.setMessage("file deleted");
        a.setFetched(false);
        a.setUserEntity(user);
        notificationRepository.save(a);
        NotificationEntity b = new NotificationEntity();
        now.add(Calendar.DATE, -1);
        b.setCreatedOn(now.getTime());
        b.setMessage("file modified");
        b.setFetched(true);
        b.setUserEntity(user);
        notificationRepository.save(b);
        List<NotificationEntity> notificationEntities = notificationRepository.findAllNonFetchedByUsername(user.getName());
        assertEquals(1, notificationEntities.size());
        assertEquals(notificationEntities.get(0), a);
    }

    @Test
    public void retrieveInReverseChronologicalOrder() {
        Calendar now = Calendar.getInstance();
        NotificationEntity a = new NotificationEntity();
        a.setCreatedOn(now.getTime());
        a.setMessage("file deleted");
        a.setFetched(false);
        a.setUserEntity(user);
        notificationRepository.save(a);
        NotificationEntity b = new NotificationEntity();
        now.add(Calendar.DATE, 1);
        b.setCreatedOn(now.getTime());
        b.setMessage("file modified");
        b.setFetched(false);
        b.setUserEntity(user);
        notificationRepository.save(b);
        List<NotificationEntity> notificationEntities = notificationRepository.findAllNonFetchedByUsername(user.getName());
        assertEquals(b, notificationEntities.get(0));
        assertEquals(a, notificationEntities.get(1));
    }

    @Test
    public void retrieveOnlyForRequestedUser() {
        Calendar now = Calendar.getInstance();
        NotificationEntity a = new NotificationEntity();
        a.setCreatedOn(now.getTime());
        a.setMessage("file deleted");
        a.setFetched(false);
        a.setUserEntity(user);
        notificationRepository.save(a);
        NotificationEntity b = new NotificationEntity();
        now.add(Calendar.DATE, 1);
        b.setCreatedOn(now.getTime());
        b.setMessage("file modified");
        b.setFetched(false);
        b.setUserEntity(otherUser);
        notificationRepository.save(b);
        List<NotificationEntity> notificationEntities = notificationRepository.findAllNonFetchedByUsername(user.getName());
        assertEquals(1, notificationEntities.size());
        assertEquals(a, notificationEntities.get(0));
    }
}