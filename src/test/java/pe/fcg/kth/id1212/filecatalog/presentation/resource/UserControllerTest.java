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
import pe.fcg.kth.id1212.filecatalog.presentation.model.LoginForm;
import pe.fcg.kth.id1212.filecatalog.presentation.model.SignUpForm;
import pe.fcg.kth.id1212.filecatalog.application.UserService;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.domain.Notification;
import pe.fcg.kth.id1212.filecatalog.domain.User;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.HttpHeaders;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private KeyGenerator keyGenerator;

    MockMvc mockMvc;

    Gson gson;

    @Before
    public void setUp() {
        mockMvc = standaloneSetup(userController).setControllerAdvice(new RestResponseEntityExceptionHandler()).build();
        gson = new Gson();
        when(keyGenerator.generateKey()).thenReturn(new SecretKeySpec("secret".getBytes(), 0, "secret".getBytes().length, "DES"));
    }

    @Test
    public void signUp_successfully() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("franco");
        signUpForm.setPassword("12345678");
        User user = new User();
        user.setId(1L);
        when(userService.save("franco", "12345678")).thenReturn(user);
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(signUpForm)))
                .andExpect(status().isCreated());
    }

    @Test
    public void signUp_whenSignUpFormNotValid() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("fcg");
        signUpForm.setPassword("pw");
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(signUpForm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", is(ErrorCode.VALIDATION.toString())))
            .andExpect(jsonPath("$.messages",
                    hasItems("The username must consist only of 4 to 8 lowercase letters",
                            "The password must be between 8 and 20 characters long")));
    }

    @Test
    public void signUp_whenUsernameIsTaken() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername("franco");
        signUpForm.setPassword("12345678");
        when(userService.save("franco", "12345678")).thenThrow(new DomainException(ErrorCode.USER_NAME_TAKEN));
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(signUpForm)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_NAME_TAKEN.toString())));
    }

    @Test
    public void logIn_successfully() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setPassword("pw");
        when(userService.findByNameAndPassword("franco", "pw")).thenReturn(new User());
        mockMvc.perform(post("/users/franco/sessions/").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(loginForm)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void logIn_incorrectPassword() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setPassword("wp");
        when(userService.findByNameAndPassword("franco", "wp")).thenThrow(new DomainException(ErrorCode.INCORRECT_PASSWORD));
        mockMvc.perform(post("/users/franco/sessions/").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(loginForm)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(ErrorCode.INCORRECT_PASSWORD.toString())));
    }

    @Test
    public void logIn_nonExistingUser() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setPassword("wp");
        when(userService.findByNameAndPassword("franco", "wp")).thenThrow(new DomainException(ErrorCode.USER_NAME_DOES_NOT_EXIST));
        mockMvc.perform(post("/users/franco/sessions/").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(loginForm)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(ErrorCode.USER_NAME_DOES_NOT_EXIST.toString())));
    }

    @Test
    public void findNotifications() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        Notification a = new Notification();
        a.setId(1L);
        a.setCreatedOn(new Date());
        a.setMessage("your file was deleted");
        Notification b = new Notification();
        b.setId(1L);
        b.setCreatedOn(new Date());
        b.setMessage("your file was modified");
        when(userService.findAllNonFetchedNotifications("franco")).thenReturn(Arrays.asList(a, b));
        mockMvc.perform(get("/users/franco/notifications/").header(HttpHeaders.AUTHORIZATION, userController.issueToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.context.entity[0].message", is("your file was deleted")))
                .andExpect(jsonPath("$.context.entity[1].message", is("your file was modified")));
    }

    @Test
    public void findNotifications_unauthorized() throws Exception {
        mockMvc.perform(get("/users/franco/notifications/").header(HttpHeaders.AUTHORIZATION, "Bearer 123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(ErrorCode.UNAUTHORIZED.toString())));
    }

    @Test
    public void findNotifications_NotUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("franco");
        mockMvc.perform(get("/users/jesus/notifications/").header(HttpHeaders.AUTHORIZATION, userController.issueToken(user)))
                .andExpect(status().isForbidden());
    }
}