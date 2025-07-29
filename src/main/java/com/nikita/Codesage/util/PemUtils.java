package com.nikita.Codesage.util;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemObject;


public class PemUtils {

    public static RSAPrivateKey readPrivateKeyFromPemFile(String classpathPath) {
        try (InputStream inputStream = PemUtils.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (inputStream == null) {
                throw new RuntimeException("PEM file not found in classpath: " + classpathPath);
            }

            PemReader pemReader = new PemReader(new InputStreamReader(inputStream));
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
            return (RSAPrivateKey) kf.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key from PEM", e);
        }
    }
}
