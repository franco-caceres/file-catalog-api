package pe.fcg.kth.id1212.filecatalog.presentation.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.fcg.kth.id1212.filecatalog.presentation.model.LoginForm;
import pe.fcg.kth.id1212.filecatalog.presentation.model.SignUpForm;
import pe.fcg.kth.id1212.filecatalog.application.UserService;
import pe.fcg.kth.id1212.filecatalog.domain.Notification;
import pe.fcg.kth.id1212.filecatalog.domain.User;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @PostMapping("/")
    public Response signUp(@Valid @RequestBody SignUpForm form, HttpServletResponse response) throws URISyntaxException {
        User user = userService.save(form.getUsername(), form.getPassword());
        response.setStatus(HttpServletResponse.SC_CREATED);
        return Response.created(new URI(user.getId().toString())).build();
    }

    @PostMapping("/{name}/sessions/")
    public Response logIn(@PathVariable String name, @RequestBody LoginForm form, HttpServletResponse response) {
        User user = userService.findByNameAndPassword(name, form.getPassword());
        String token = issueToken(user);
        response.setHeader(HttpHeaders.AUTHORIZATION, token);
        return Response.ok(user).header(HttpHeaders.AUTHORIZATION, token).build();
    }

    @GetMapping("/{name}/notifications/")
    public Response findNotifications(@PathVariable String name, HttpServletResponse response) {
        String username = getUsernameFromRequest();
        if(!Objects.equals(username, name)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        List<Notification> notifications = userService.findAllNonFetchedNotifications(name);
        return Response.ok(notifications).build();
    }
}
