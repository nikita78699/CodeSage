package com.nikita.Codesage.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PemUtils {

    public static RSAPrivateKey readPrivateKeyFromPemFile(String pemFilePath) {
        try {
            InputStream is = PemUtils.class.getClassLoader().getResourceAsStream(pemFilePath);
            if (is == null) {
                throw new RuntimeException("PEM file not found: " + pemFilePath);
            }

            String privateKeyPEM = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .filter(line -> !line.startsWith("-----"))
                    .collect(Collectors.joining());

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key from PEM", e);
        }
    }
}
