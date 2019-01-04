package pe.fcg.kth.id1212.filecatalog.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.entity.NotificationEntity;
import pe.fcg.kth.id1212.filecatalog.entity.UserEntity;
import pe.fcg.kth.id1212.filecatalog.repository.NotificationRepository;
import pe.fcg.kth.id1212.filecatalog.repository.UserRepository;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;
import pe.fcg.kth.id1212.filecatalog.util.Util;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    private UserEntity user;

    @Before
    public void setUp() {
        user = new UserEntity();
        user.setName("franco");
        user.setPassword(Util.getPasswordHash("franco", "pw"));
        userRepository.save(user);
    }

    @Test
    public void save_whenUserWithAlreadyUsedName() {
        when(userRepository.findByName("franco")).thenReturn(new UserEntity());
        try {
            userService.save("franco", "wp");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.USER_NAME_TAKEN, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void findByNameAndPassword_whenNameDoesNotExist() {
        when(userRepository.findByName("franco")).thenReturn(null);
        try {
            userService.findByNameAndPassword("franco", "pw");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.USER_NAME_DOES_NOT_EXIST, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void findByNameAndHashedPassword_whenPasswordIsWrong() {
        when(userRepository.findByName("franco")).thenReturn(user);
        try {
            userService.findByNameAndPassword("franco", "wp");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.INCORRECT_PASSWORD, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void findByName_whenNameDoesNotExist() {
        when(userRepository.findByName("franco")).thenReturn(null);
        try {
            userService.findByName("franco");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.USER_NAME_DOES_NOT_EXIST, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void saveNotificationForUser_whenUserDoesNotExist() {
        when(userRepository.findByName("franco")).thenReturn(null);
        try {
            userService.saveNotificationForUser("franco", "your file was deleted");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.USER_NAME_DOES_NOT_EXIST, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void setFetchedAfter_findAllNonFetchedNotifications() {
        NotificationEntity a = new NotificationEntity();
        a.setFetched(false);
        a.setMessage("your file was deleted");
        a.setCreatedOn(new Date());
        when(notificationRepository.findAllNonFetchedByUsername("franco")).thenReturn(Collections.singletonList(a));
        userService.findAllNonFetchedNotifications("franco");
        assertTrue(a.isFetched());
    }
}