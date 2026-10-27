package com.manuelhg.aad;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Service
public class CryptoService {

    private static final String SECRET = "AadTexSecret2026"; // 16 caracteres = 16 bytes = AES-128
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
        return process(Cipher.DECRYPT_MODE, encrypted);
    }

    public byte[] encrypt(byte[] plain) throws GeneralSecurityException {
        return process(Cipher.ENCRYPT_MODE, plain);
    }

    private byte[] process(int mode, byte[] data) throws GeneralSecurityException {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, key);
        return cipher.doFinal(data);
    }
}



