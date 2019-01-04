package pe.fcg.kth.id1212.filecatalog.util;

import org.apache.commons.beanutils.BeanUtils;

import java.security.MessageDigest;

public class Util {
    public static <T> T copyBeanProperties(T dest, Object orig) {
        try {
            if (orig != null) {
                BeanUtils.copyProperties(dest, orig);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return dest;
    }

    public static String getPasswordHash(String username, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            return new String(md.digest());
        } catch(Exception e) {
            return null;
        }
    }
}
