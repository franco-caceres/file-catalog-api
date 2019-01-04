package pe.fcg.kth.id1212.filecatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.fcg.kth.id1212.filecatalog.entity.FileEntity;

@Transactional(propagation = Propagation.MANDATORY)
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    FileEntity findByName(String name);
}
