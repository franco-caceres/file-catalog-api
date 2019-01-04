package pe.fcg.kth.id1212.filecatalog.application;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.domain.Notification;
import pe.fcg.kth.id1212.filecatalog.domain.User;
import pe.fcg.kth.id1212.filecatalog.entity.NotificationEntity;
import pe.fcg.kth.id1212.filecatalog.entity.UserEntity;
import pe.fcg.kth.id1212.filecatalog.repository.NotificationRepository;
import pe.fcg.kth.id1212.filecatalog.repository.UserRepository;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;
import pe.fcg.kth.id1212.filecatalog.util.Util;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    public User save(String name, String password) {
        UserEntity existingEntity = userRepository.findByName(name);
        if(existingEntity != null) {
            throw new DomainException(ErrorCode.USER_NAME_TAKEN);
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setPassword(Util.getPasswordHash(name, password));
        userRepository.save(userEntity);
        return makeFromEntity(userEntity);
    }

    public User findByNameAndPassword(String name, String password) {
        UserEntity userEntity = userRepository.findByName(name);
        if(userEntity == null) {
            throw new DomainException(ErrorCode.USER_NAME_DOES_NOT_EXIST);
        } else if(!Objects.equals(Util.getPasswordHash(name, password), userEntity.getPassword())) {
            throw new DomainException(ErrorCode.INCORRECT_PASSWORD);
        }
        return makeFromEntity(userEntity);
    }

    public User findByName(String name) {
        UserEntity userEntity = userRepository.findByName(name);
        if(userEntity == null) {
            throw new DomainException(ErrorCode.USER_NAME_DOES_NOT_EXIST);
        }
        return makeFromEntity(userEntity);
    }

    public List<Notification> findAllNonFetchedNotifications(String username) {
        List<NotificationEntity> notificationEntities = notificationRepository.findAllNonFetchedByUsername(username);
        notificationEntities.forEach(x -> x.setFetched(true));
        return notificationEntities.stream().map(this::makeFromEntity).collect(Collectors.toList());
    }

    public void saveNotificationForUser(String username, String message) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setCreatedOn(new Date());
        notificationEntity.setMessage(message);
        notificationEntity.setFetched(false);
        UserEntity userEntity = userRepository.findByName(username);
        if(userEntity == null) {
            throw new DomainException(ErrorCode.USER_NAME_DOES_NOT_EXIST);
        }
        notificationEntity.setUserEntity(userEntity);
        notificationRepository.save(notificationEntity);
    }

    Notification makeFromEntity(NotificationEntity entity) {
        if(entity == null) {
            return null;
        }
        return Util.copyBeanProperties(new Notification(), entity);
    }

    User makeFromEntity(UserEntity entity) {
        if(entity == null) {
            return null;
        }
        return Util.copyBeanProperties(new User(), entity);
    }

    UserEntity makeFromModel(User model) {
        if(model == null) {
            return null;
        }
        return Util.copyBeanProperties(new UserEntity(), model);
    }
}
