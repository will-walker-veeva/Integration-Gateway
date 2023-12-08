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

    @Autowired
    public void setAppConfiguration(AppConfiguration appConfiguration){
        EncryptionClient.appConfiguration = appConfiguration;
        init();
    }

    public EncryptionClient(){
        try{
            init();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void init() {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(appConfiguration.getDecryptionToken());
        encryptor.setAlgorithm(algorithm);
        encryptor.setIvGenerator(new RandomIvGenerator());
    }

    public String encrypt(String message) {
        Objects.requireNonNull(message, "Message must not be null");
        return encryptor.encrypt(message);
    }

    public String decrypt(String message) {
        Objects.requireNonNull(message, "Message must not be null");
        return encryptor.decrypt(message);
    }
}
