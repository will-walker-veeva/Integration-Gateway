package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.AppConfiguration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EncryptionClient {

    private static AppConfiguration appConfiguration;
    private String algorithm = "PBEWithHMACSHA512AndAES_256";
    private StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

    /**
     * @hidden
     * @param appConfiguration
     */
    @Autowired
    public void setAppConfiguration(AppConfiguration appConfiguration){
        EncryptionClient.appConfiguration = appConfiguration;
        init();
    }

    /**
     * @hidden
     */
    public EncryptionClient(){
        try{
            init();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void init() {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(appConfiguration.getDecryptionToken());
        encryptor.setAlgorithm(algorithm);
        encryptor.setIvGenerator(new RandomIvGenerator());
    }

    /**
     * Encrypts the given message using the application's security key
     * @param message
     * @return The encrypted message
     */
    public String encrypt(String message) {
        Objects.requireNonNull(message, "Message must not be null");
        return encryptor.encrypt(message);
    }

    /**
     * Decrypts the given message using the application's security key
     * @param message
     * @return The decrypted message
     */
    public String decrypt(String message) {
        Objects.requireNonNull(message, "Message must not be null");
        return encryptor.decrypt(message);
    }
}
