package pe.fcg.kth.id1212.filecatalog.presentation.resource;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.fcg.kth.id1212.filecatalog.presentation.ApiException;
import pe.fcg.kth.id1212.filecatalog.presentation.KeyGenerator;
import pe.fcg.kth.id1212.filecatalog.domain.User;
import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
public class BaseController {
    @Autowired
    private ServletContext servletContext;

    @Autowired
    KeyGenerator keyGenerator;

    String getUsernameFromRequest() {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
        String auth = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            String token = auth.substring("Bearer".length()).trim();
            Key key = keyGenerator.generateKey();
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    String issueToken(User user) {
        final Instant instant = LocalDateTime
                .now()
                .plusDays((long) 1)
                .atZone(ZoneId.systemDefault())
                .toInstant();
        return "Bearer " + Jwts.builder()
                .setSubject(user.getName())
                .setIssuer(servletContext.getContextPath())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(instant))
                .signWith(SignatureAlgorithm.HS512, keyGenerator.generateKey())
                .compact();
    }
}
