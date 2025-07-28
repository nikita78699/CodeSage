package com.nikita.Codesage.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;

public class GitHubAppTokenUtil {

    public static String generateJWT(String appId, RSAPrivateKey privateKey) {
        long now = Instant.now().getEpochSecond();
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);

        return JWT.create()
            .withIssuer(appId)
            .withIssuedAt(new Date(now * 1000))
            .withExpiresAt(new Date((now + 540) * 1000)) // 9 min expiry
            .sign(algorithm);
    }
}
