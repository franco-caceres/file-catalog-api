package pe.fcg.kth.id1212.filecatalog.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.domain.File;
import pe.fcg.kth.id1212.filecatalog.domain.User;
import pe.fcg.kth.id1212.filecatalog.entity.FileEntity;
import pe.fcg.kth.id1212.filecatalog.entity.UserEntity;
import pe.fcg.kth.id1212.filecatalog.repository.FileRepository;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileServiceTest {
    @Autowired
    private FileService fileService;

    @MockBean
    private FileRepository fileRepository;

    @Test
    public void findByIdNonExistingFile() {
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());
        try {
            fileService.findById(1L);
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.NOT_FOUND, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void saveExistingFile_WhenFileIsReadOnlyAndRequesterIsNotOwner() {
        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setName("franco");
        owner.setPassword("pw");
        FileEntity existingFile = new FileEntity();
        existingFile.setId(1L);
        existingFile.setSize(10);
        existingFile.setContent(new byte[10]);
        existingFile.setName("pic.jpg");
        existingFile.setReadOnly(true);
        existingFile.setUserEntity(owner);
        when(fileRepository.findByName("pic.jpg")).thenReturn(existingFile);
        File toSave = new File();
        toSave.setSize(20);
        toSave.setName("pic.jpg");
        toSave.setReadOnly(false);
        toSave.setContent(new byte[20]);
        try {
            fileService.save(toSave, "otherUser");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.USER_NOT_OWNER, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void saveExistingFile_WhenFileIsReadOnlyButRequesterIsOwner() {
        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setName("franco");
        owner.setPassword("pw");
        FileEntity existingFile = new FileEntity();
        existingFile.setId(1L);
        existingFile.setSize(10);
        existingFile.setContent(new byte[10]);
        existingFile.setName("pic.jpg");
        existingFile.setReadOnly(true);
        existingFile.setUserEntity(owner);
        when(fileRepository.findByName("pic.jpg")).thenReturn(existingFile);
        File toSave = new File();
        toSave.setSize(20);
        toSave.setName("pic.jpg");
        toSave.setReadOnly(false);
        toSave.setContent(new byte[20]);
        fileService.save(toSave, "franco");
    }

    @Test
    public void deleteNonExistingFile() {
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());
        try {
            fileService.delete(1L, "franco");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.NOT_FOUND, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void deleteFile_WhenFileIsReadOnlyAndRequesterIsNotOwner() {
        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setName("franco");
        owner.setPassword("pw");
        FileEntity existingFile = new FileEntity();
        existingFile.setId(1L);
        existingFile.setSize(10);
        existingFile.setContent(new byte[10]);
        existingFile.setName("pic.jpg");
        existingFile.setReadOnly(true);
        existingFile.setUserEntity(owner);
        when(fileRepository.findById(1L)).thenReturn(Optional.of(existingFile));
        try {
            fileService.delete(1L, "otherUser");
            fail("Should have thrown exception");
        } catch(DomainException de) {
            assertEquals(ErrorCode.USER_NOT_OWNER, de.getCode());
        } catch(Exception e) {
            fail("Should have thrown DomainException");
        }
    }

    @Test
    public void deleteFile_WhenFileIsReadOnlyButRequesterIsOwner() {
        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setName("franco");
        owner.setPassword("pw");
        FileEntity existingFile = new FileEntity();
        existingFile.setId(1L);
        existingFile.setSize(10);
        existingFile.setContent(new byte[10]);
        existingFile.setName("pic.jpg");
        existingFile.setReadOnly(true);
        existingFile.setUserEntity(owner);
        when(fileRepository.findById(1L)).thenReturn(Optional.of(existingFile));
        fileService.delete(1L, "franco");
    }

    @Test
    public void onlyCopyModifiableFields() {
        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setName("franco");
        owner.setPassword("pw");
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(1L);
        fileEntity.setSize(10);
        fileEntity.setContent(new byte[10]);
        fileEntity.setName("pic.jpg");
        fileEntity.setReadOnly(true);
        fileEntity.setUserEntity(owner);
        File changedFile = new File();
        changedFile.setId(1L);
        changedFile.setSize(20);
        changedFile.setContent(new byte[20]);
        changedFile.setName("pic2.jpg");
        changedFile.setReadOnly(false);
        changedFile.setUser(new User());
        fileService.copyModifiableFields(fileEntity, changedFile);
        assertEquals(1L, fileEntity.getId().longValue());
        assertEquals("pic.jpg", fileEntity.getName());
        assertEquals(owner, fileEntity.getUserEntity());
        assertEquals(changedFile.getSize(), fileEntity.getSize());
        assertEquals(changedFile.getContent(), fileEntity.getContent());
        assertEquals(changedFile.isReadOnly(), fileEntity.isReadOnly());
    }
}