package pe.fcg.kth.id1212.filecatalog.application;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.domain.File;
import pe.fcg.kth.id1212.filecatalog.domain.User;
import pe.fcg.kth.id1212.filecatalog.entity.FileEntity;
import pe.fcg.kth.id1212.filecatalog.repository.FileRepository;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;
import pe.fcg.kth.id1212.filecatalog.util.Util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserService userService;

    public List<File> findAll() {
        List<FileEntity> fileEntities = fileRepository.findAll();
        List<File> files = fileEntities.stream().map(this::makeFromEntity).collect(Collectors.toList());
        files.forEach(f -> f.setContent(null));
        return files;
    }

    public File findById(Long id) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(id);
        if(fileEntityOptional.isPresent()) {
            FileEntity fileEntity = fileEntityOptional.get();
            return makeFromEntity(fileEntity);
        } else {
            throw new DomainException(ErrorCode.NOT_FOUND);
        }
    }

    public File save(File file, String username) {
        FileEntity existingEntity = fileRepository.findByName(file.getName());
        if(existingEntity != null) {
            boolean userIsOwner = Objects.equals(username, existingEntity.getUserEntity().getName());
            if(existingEntity.isReadOnly() && !userIsOwner) {
                throw new DomainException(ErrorCode.USER_NOT_OWNER);
            } else {
                copyModifiableFields(existingEntity, file);
                fileRepository.save(existingEntity);
                if(!userIsOwner) {
                    userService.saveNotificationForUser(existingEntity.getUserEntity().getName(),
                            String.format("%s has updated a file you own (%s)", username, existingEntity.getName()));
                }
                return makeFromEntity(existingEntity);
            }
        } else {
            FileEntity fileEntity = makeFromModel(file);
            User user = userService.findByName(username);
            fileEntity.setUserEntity(userService.makeFromModel(user));
            fileRepository.save(fileEntity);
            return makeFromEntity(fileEntity);
        }
    }

    public void delete(Long id, String username) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(id);
        if(fileEntityOptional.isPresent()) {
            FileEntity fileEntity = fileEntityOptional.get();
            boolean userIsOwner = Objects.equals(fileEntity.getUserEntity().getName(), username);
            if(fileEntity.isReadOnly() && !userIsOwner) {
                throw new DomainException(ErrorCode.USER_NOT_OWNER);
            } else {
                if(!userIsOwner) {
                    userService.saveNotificationForUser(fileEntity.getUserEntity().getName(),
                            String.format("%s has deleted a file you own (%s)", username, fileEntity.getName()));
                }
                fileRepository.deleteById(id);
            }
        } else {
            throw new DomainException(ErrorCode.NOT_FOUND);
        }
    }

    void copyModifiableFields(FileEntity dest, File orig) {
        dest.setContent(orig.getContent());
        dest.setSize(orig.getSize());
        dest.setReadOnly(orig.isReadOnly());
    }

    File makeFromEntity(FileEntity entity) {
        if(entity == null) {
            return null;
        }
        File model = Util.copyBeanProperties(new File(), entity);
        model.setReadOnly(entity.isReadOnly());
        model.setUser(userService.makeFromEntity(entity.getUserEntity()));
        return model;
    }

    FileEntity makeFromModel(File model) {
        if(model == null) {
            return null;
        }
        FileEntity entity = Util.copyBeanProperties(new FileEntity(), model);
        entity.setReadOnly(model.isReadOnly());
        entity.setUserEntity(userService.makeFromModel(model.getUser()));
        return entity;
    }
}
