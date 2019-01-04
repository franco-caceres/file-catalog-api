package pe.fcg.kth.id1212.filecatalog.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@ApplicationScope
@Component
public class SimpleKeyGenerator implements KeyGenerator {
    @Value("#{systemProperties['file-catalog-key'] ?: 'simplekey'}")
    private String keyString;

    @Override
    public Key generateKey() {
        return new SecretKeySpec(keyString.getBytes(), 0, keyString.getBytes().length, "DES");
    }
}
