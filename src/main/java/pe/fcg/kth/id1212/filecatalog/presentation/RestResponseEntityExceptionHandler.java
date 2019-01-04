package pe.fcg.kth.id1212.filecatalog.presentation;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pe.fcg.kth.id1212.filecatalog.domain.DomainException;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = { DomainException.class })
    protected ResponseEntity<Object> handleDomainException(DomainException ex, WebRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", ex.getCode().toString());
        HttpStatus status;
        switch(ex.getCode()) {
            case NOT_FOUND:
            case USER_NAME_DOES_NOT_EXIST:
                status = HttpStatus.NOT_FOUND;
                break;
            default:
                status = HttpStatus.FORBIDDEN;
        }
        return handleExceptionInternal(ex, map, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = { ApiException.class })
    protected ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", ex.getCode().toString());
        return handleExceptionInternal(ex, map, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", ErrorCode.VALIDATION.toString());
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        map.put("messages", errorMessages);
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("code", ErrorCode.UNKNOWN.toString());
        return handleExceptionInternal(ex, map, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}