package com.fxlso.util;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class JwtKeyGen {
    public static void main(String[] args) {
        String base64Secret = Encoders.BASE64.encode(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded());
        System.out.println(base64Secret);
    }
}
