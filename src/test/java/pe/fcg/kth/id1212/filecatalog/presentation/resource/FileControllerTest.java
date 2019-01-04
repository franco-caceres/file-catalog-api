package pe.fcg.kth.id1212.filecatalog.presentation.resource;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pe.fcg.kth.id1212.filecatalog.presentation.KeyGenerator;
import pe.fcg.kth.id1212.filecatalog.presentation.RestResponseEntityExceptionHandler;
import pe.fcg.kth.id1212.filecatalog.presentation.model.UploadForm;
import pe.fcg.kth.id1212.filecatalog.application.FileService;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.domain.File;
import pe.fcg.kth.id1212.filecatalog.domain.User;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.HttpHeaders;

import java.util.Arrays;
import java.util.Base64;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileControllerTest {

    @Autowired
    private FileController fileController;

    @MockBean
    private FileService fileService;

    @MockBean
    private KeyGenerator keyGenerator;

    MockMvc mockMvc;

    Gson gson;

    @Before
    public void setUp() {
        mockMvc = standaloneSetup(fileController).setControllerAdvice(new RestResponseEntityExceptionHandler()).build();
        gson = new Gson();
        when(keyGenerator.generateKey()).thenReturn(new SecretKeySpec("secret".getBytes(), 0, "secret".getBytes().length, "DES"));
    }

    @Test
    public void findAll_unauthorized() throws Exception {
        mockMvc.perform(get("/files/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(ErrorCode.UNAUTHORIZED.toString())));
    }

    @Test
    public void findAll_successfully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        File a = new File();
        a.setId(1L);
        a.setName("a.jpg");
        a.setContent("drawing".getBytes());
        a.setReadOnly(true);
        File b = new File();
        b.setId(1L);
        b.setName("b.pdf");
        b.setContent("essay".getBytes());
        b.setReadOnly(false);
        when(fileService.findAll()).thenReturn(Arrays.asList(a, b));
        String expectedAContent = new String(Base64.getEncoder().encode(a.getContent()));
        String expectedBContent = new String(Base64.getEncoder().encode(b.getContent()));
        mockMvc.perform(get("/files/").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.context.entity[0].id", is(Integer.valueOf(a.getId().toString()))))
                .andExpect(jsonPath("$.context.entity[0].name", is(a.getName())))
                .andExpect(jsonPath("$.context.entity[0].content", is(expectedAContent)))
                .andExpect(jsonPath("$.context.entity[0].readOnly", is(a.isReadOnly())))
                .andExpect(jsonPath("$.context.entity[1].id", is(Integer.valueOf(b.getId().toString()))))
                .andExpect(jsonPath("$.context.entity[1].name", is(b.getName())))
                .andExpect(jsonPath("$.context.entity[1].content", is(expectedBContent)))
                .andExpect(jsonPath("$.context.entity[1].readOnly", is(b.isReadOnly())));
    }

    @Test
    public void findById_successfully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        File a = new File();
        a.setId(1L);
        a.setName("a.jpg");
        a.setContent("drawing".getBytes());
        a.setReadOnly(true);
        when(fileService.findById(1L)).thenReturn(a);
        String expectedAContent = new String(Base64.getEncoder().encode(a.getContent()));
        mockMvc.perform(get("/files/1").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.context.entity.id", is(Integer.valueOf(a.getId().toString()))))
                .andExpect(jsonPath("$.context.entity.name", is(a.getName())))
                .andExpect(jsonPath("$.context.entity.content", is(expectedAContent)))
                .andExpect(jsonPath("$.context.entity.readOnly", is(a.isReadOnly())));
    }

    @Test
    public void findById_nonExisting() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        when(fileService.findById(1L)).thenThrow(new DomainException(ErrorCode.NOT_FOUND));
        mockMvc.perform(get("/files/1").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(ErrorCode.NOT_FOUND.toString())));
    }

    @Test
    public void upload_whenUploadFormIsNotValid() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        UploadForm uploadForm = new UploadForm();
        uploadForm.setName(".txt");
        uploadForm.setSize(0);
        uploadForm.setReadOnly(true);
        uploadForm.setContent(new byte[0]);
        mockMvc.perform(post("/files/").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)).contentType(MediaType.APPLICATION_JSON).content(gson.toJson(uploadForm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(ErrorCode.VALIDATION.toString())))
                .andExpect(jsonPath("$.messages",
                        hasItems("The file name must have between 5 and 30 characters",
                                "The file must be at least 1 byte")));
    }

    @Test
    public void uploadFails_whenFileIsReadOnlyAndRequesterIsNotOwner() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        when(fileService.save(any(), any())).thenThrow(new DomainException(ErrorCode.USER_NOT_OWNER));
        UploadForm uploadForm = new UploadForm();
        uploadForm.setName("a.txt");
        uploadForm.setSize(5);
        uploadForm.setReadOnly(false);
        uploadForm.setContent(new byte[5]);
        mockMvc.perform(post("/files/").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)).contentType(MediaType.APPLICATION_JSON).content(gson.toJson(uploadForm)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_NOT_OWNER.toString())));
    }

    @Test
    public void upload_successFully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        UploadForm uploadForm = new UploadForm();
        uploadForm.setName("a.txt");
        uploadForm.setSize(5);
        uploadForm.setReadOnly(false);
        uploadForm.setContent(new byte[5]);
        mockMvc.perform(post("/files/").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)).contentType(MediaType.APPLICATION_JSON).content(gson.toJson(uploadForm)))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_successfully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        mockMvc.perform(delete("/files/1").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_nonExistingFile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        doThrow(new DomainException(ErrorCode.NOT_FOUND)).when(fileService).delete(any(), any());
        mockMvc.perform(delete("/files/1").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(ErrorCode.NOT_FOUND.toString())));
    }

    @Test
    public void delete_whenFileIsReadOnlyAndRequesterIsNotOwner() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        doThrow(new DomainException(ErrorCode.USER_NOT_OWNER)).when(fileService).delete(any(), any());
        mockMvc.perform(delete("/files/1").header(HttpHeaders.AUTHORIZATION, fileController.issueToken(user)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_NOT_OWNER.toString())));
    }
}