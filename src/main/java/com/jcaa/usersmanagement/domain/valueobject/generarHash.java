
package com.jcaa.usersmanagement.domain.valueobject;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class generarHash {
    public static void main(String[] args) {
        String password = "admin123";

        String hash = "$2a$12$iQ4HQjWlR86E.HHPyLt//etXAe7WFxqrQUrL0pMK0ckXF/bPCd/yK";

        BCrypt.Result resultado = BCrypt.verifyer().verify(
            password.toCharArray(),
            hash
        );

        System.out.println("¿La contraseña coincide?: " + resultado.verified);
    }
}
