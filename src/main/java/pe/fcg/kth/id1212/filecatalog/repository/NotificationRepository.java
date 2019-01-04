package pe.fcg.kth.id1212.filecatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.fcg.kth.id1212.filecatalog.entity.NotificationEntity;

import java.util.List;

@Transactional(propagation = Propagation.MANDATORY)
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("select x from NotificationEntity x where x.userEntity.name=:username and x.fetched=false order by x.createdOn desc")
    List<NotificationEntity> findAllNonFetchedByUsername(@Param("username") String username);
}
