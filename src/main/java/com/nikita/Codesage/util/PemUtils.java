package com.nikita.Codesage.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class PemUtils {

    public static RSAPrivateKey readPrivateKeyFromPemFile(String classpathPath) {
        try (InputStream inputStream = PemUtils.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (inputStream == null) {
                throw new RuntimeException("PEM file not found in classpath: " + classpathPath);
            }

            try (PemReader pemReader = new PemReader(new InputStreamReader(inputStream))) {
                PemObject pemObject = pemReader.readPemObject();
                byte[] pkcs1Bytes = pemObject.getContent();
                byte[] pkcs8Bytes = convertPKCS1ToPKCS8(pkcs1Bytes);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
                PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
                return (RSAPrivateKey) privateKey;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key from PEM", e);
        }
    }

    private static byte[] convertPKCS1ToPKCS8(byte[] pkcs1Bytes) {
        try {
            // Convert PKCS#1 format to PKCS#8 format
            // use fully qualified class name to avoid import conflict
            org.bouncycastle.asn1.pkcs.RSAPrivateKey bcPrivateKey =
                    org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(pkcs1Bytes);

            AlgorithmIdentifier algId = new AlgorithmIdentifier(
                    PKCSObjectIdentifiers.rsaEncryption,
                    DERNull.INSTANCE
            );

            PrivateKeyInfo privKeyInfo = new PrivateKeyInfo(algId, bcPrivateKey);
            return privKeyInfo.getEncoded();

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert PKCS#1 to PKCS#8", e);
        }
    }
}
