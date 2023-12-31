package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.AppConfiguration;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.files.File;

import org.apache.commons.mail.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailClient {
    private static AppConfiguration appConfiguration;

    /**
     * @hidden 
     */
    public EmailClient(){

    }

    /**
     * @hidden
     * @param appConfiguration
     */
    @Autowired
    public void setAppConfiguration(AppConfiguration appConfiguration){
        EmailClient.appConfiguration = appConfiguration;
    }

    @Autowired
    EncryptionClient encryptionClient;

    /**
     * Sends an email tp the given email address with the given subject and body
     * @param emailAddress
     * @param subject
     * @param body
     * @throws ProcessException
     */
    public void sendNotification(String emailAddress, String subject, String body)  throws ProcessException {
        try{
            Email email = new SimpleEmail();
            email.setHostName(appConfiguration.getEmailHost());
            email.setSmtpPort(appConfiguration.getEmailPort());
            email.setAuthenticator(new DefaultAuthenticator(appConfiguration.getEmailUsername(), getDecryptedPassword(appConfiguration.getEmailPassword())));
            email.setSSLOnConnect(true);
            email.setSubject(subject);
            email.setMsg(body);
            email.addTo(emailAddress);
            email.send();
        }catch (Exception e){
            throw new ProcessException(e.getMessage());
        }

    }

    /**
     * Sends an email tp the given email address with the given subject, body and attachment
     * @param emailAddress
     * @param subject
     * @param body
     * @param file
     * @throws ProcessException
     */
    public void sendNotification(String emailAddress, String subject, String body, File file) throws ProcessException {
        try{
            MultiPartEmail email = new MultiPartEmail();
            email.setHostName(appConfiguration.getEmailHost());
            email.setSmtpPort(appConfiguration.getEmailPort());
            email.setAuthenticator(new DefaultAuthenticator(appConfiguration.getEmailUsername(), getDecryptedPassword(appConfiguration.getEmailPassword())));
            email.setSSLOnConnect(true);
            email.setSubject(subject);
            email.setMsg(body);
            email.addTo(emailAddress);
            if(file!=null){
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(file.getAbsolutePath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                email.attach(attachment);
            }
            email.send();
        }catch (Exception e){
            throw new ProcessException(e.getMessage());
        }

    }

    /**
     * @hidden
     * @param encryptedPassword
     * @return
     */
    private String getDecryptedPassword(String encryptedPassword){
        return encryptionClient.decrypt(encryptedPassword);
    }
}
