package com.manuelhg.aad.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;


//Esta clase se encarga de recibir y pasar bytes. Llegan datos, cifra con AES-128, y envía de vuelta.
//La separamos de Scheduler para que este no tenga que lidiar con nada de criptografía.


@Service
//@Service es igual que @Component, pero indica que contiene lógica de negocio. Spring crea
// un bean, y lo comparte con quien lo necesite.
public class CryptoService {

    private static final String SECRET = "AadTexSecret2026"; // 16 caracteres = 16 bytes = AES-128
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    //Son static y final porque son constantes, no cambian, no dependen de ningún objeto.


    //throws GeneralSecurityException significa que puede fallar (ej: archivoc orrupto)
    //decrypt se usa el scheduler para los .enc
    //encrypt lo usa MultiLanguageDataGenerator para fabricar los .enc de prueba

    public byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
        return process(Cipher.DECRYPT_MODE, encrypted);
    }

    public byte[] encrypt(byte[] plain) throws GeneralSecurityException {
        return process(Cipher.ENCRYPT_MODE, plain);
    }

    private byte[] process(int mode, byte[] data) throws GeneralSecurityException {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "AES");
        //Aquí envolvemos los bytes en un objeto. SECRET.getBytes convierte el texto de la clave en bytes.
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        //Cipher le pide a java una máquina para cifrar, con lo establecido en la String ALGORITHM.
        cipher.init(mode, key);
        return cipher.doFinal(data);
    }
}



