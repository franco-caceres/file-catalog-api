package pe.fcg.kth.id1212.filecatalog.presentation.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.fcg.kth.id1212.filecatalog.presentation.model.UploadForm;
import pe.fcg.kth.id1212.filecatalog.application.FileService;
import pe.fcg.kth.id1212.filecatalog.domain.File;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/files")
public class FileController extends BaseController {
    @Autowired
    private FileService fileService;

    @GetMapping("/")
    public Response findAll() {
        getUsernameFromRequest();
        return Response.ok().entity(fileService.findAll()).build();
    }

    @GetMapping("/{id}")
    public Response findById(@PathVariable long id) {
        getUsernameFromRequest();
        return Response.ok().entity(fileService.findById(id)).build();
    }

    @PostMapping("/")
    public Response upload(@Valid @RequestBody UploadForm form, HttpServletResponse response) throws URISyntaxException {
        String username = getUsernameFromRequest();
        File file = new File();
        file.setName(form.getName());
        file.setSize(form.getSize());
        file.setContent(form.getContent());
        file.setReadOnly(form.isReadOnly());
        fileService.save(file, username);
        return Response.ok().build();
    }

    @DeleteMapping("/{id}")
    public Response delete(@PathVariable long id) {
        String username = getUsernameFromRequest();
        fileService.delete(id, username);
        return Response.ok().build();
    }
}
