package com.veeva.vault.custom.app;

import com.veeva.vault.custom.app.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;
import java.util.*;

import static com.veeva.vault.custom.app.client.Client.VAULT_CLIENT_ID;

public class SparkUtilities {
    /*
     * getPublicKey makes a REST API call to the Vault HTTPS REST API to retrieve the certificate/pem file
     * @param sessionId, an active sessionId for the vault
     * @param certificateId, the id of the certificate/pem to be retrieved
     * @param hostname, the vault hostname
     * @returns certificate, the certificate retrieved from the vault
     */
    public static String getPublicKey(String hostname, String apiVersion, String sessionId, String certificateId) throws Exception {
        final Logger logger = LogManager.getLogger();
        byte[] publicKey = getCertificate(hostname, apiVersion, sessionId, certificateId);
        return new String(publicKey);
    }

    public static byte[] getCertificate(String domain, String apiVersion, String sessionId, String certificateId) throws Exception {
        String url = String.format("https://%s/api/%s/services/certificate/%s", domain, apiVersion, certificateId);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("X-VaultAPI-ClientID", VAULT_CLIENT_ID);
        headers.put("Authorization", sessionId);
        byte[] response = HttpClient.newInstance().get(url, headers, byte[].class);
        return response;
    }

    /*
     * validates the message sent to the lambda function using the public key
     * @param headers, the map containing the headers received
     * @param body, the body received as a json string
     * @param publicKey, the public key used to verify the message
     * @returns verified, an boolean representing whether the spark message was valid
     */
    public static boolean validate(Map<String,String> headers, String body, PublicKey publicKey) {
        final Logger logger = LogManager.getLogger();
        String stringToVerify = null;

        // Create the String-to-Verify
        stringToVerify = prepareDataToVerify(headers, body);

        // Get the X-Vault-API-SignatureV2
        String xVaultAPISignature = "";
        if(headers.get("X-VaultAPI-SignatureV2") != null) {
            xVaultAPISignature = headers.get("X-VaultAPI-SignatureV2");
        } else if (headers.get("x-vaultapi-signaturev2") != null) {
            xVaultAPISignature = headers.get("x-vaultapi-signaturev2");
        }
        logger.info("Signature: " + xVaultAPISignature);

        // Verify the spark message
        boolean verified = verifySignUsingCrypto(stringToVerify,xVaultAPISignature, publicKey);
        logger.info("Verified: " + verified);
        return verified;
    }

    /*
     * prepareDataToVerify prepares the string-to-verify/string-to-sign
     * @param headers, the map containing the headers received
     * @param body, the body received as a json string
     * @returns stringToVerify, the string used to verify the spark message
     */
    public static String prepareDataToVerify(Map<String,String> headers, String body) {
        final Logger logger = LogManager.getLogger();
        // Create set of X-VaultAPISignature-* headers
        Set<String> filteredHeaderSet = new HashSet<>();
        for(String key: headers.keySet()){
            if(key.indexOf("X-VaultAPISignature-") == 0 || key.indexOf("x-vaultapisignature-") == 0) {
                filteredHeaderSet.add(key);
            }
        }
        // Sort X-VaultAPISignature-* headers alphabetically
        String[] headerKeysArray = filteredHeaderSet.stream().toArray(String[]::new);
        Arrays.sort(headerKeysArray);

        // Convert X-VaultAPISignature-* headers to Lowercase(<HeaderName1>)+":"+Trim(<value>)+"\n" format
        int headerKeysArrayLength = headerKeysArray.length;
        for(int i = 0; i < headerKeysArrayLength; i++) {
            String key = headerKeysArray[i];
            headerKeysArray[i] = key.toLowerCase() + ":" + headers.get(key).trim() + "\n";
        }

        /*
         * The String-to-verify must be in the following format:
         * All X-VaultAPISignature-* headers in the request must be in the following format: Lowercase(<HeaderName1>)+":"+Trim(<value>)+"\n"
         * Each header name-value pair must be separated by the newline character (\n)
         * Header names must be in lower case
         * Header name-value pairs must not contain any spaces
         * Header names must be sorted alphabetically
         * The JSON object in the HTTP body of the request must be raw text
         * Add a newline character after the HTTP body, followed by the full HTTPS URL as received by your external service.
         * Make sure this also includes any query parameters.
         */
        StringBuilder stringToVerifySB = new StringBuilder(String.join("", headerKeysArray))
                .append(body)
                .append("\n");
        if (headers.get("X-VaultAPISignature-URL") != null) {
            stringToVerifySB.append(headers.get("X-VaultAPISignature-URL"));
        } else if(headers.get("x-vaultapisignature-url") != null) {
            stringToVerifySB.append(headers.get("x-vaultapisignature-url"));
        }

        logger.info(stringToVerifySB.toString());
        return stringToVerifySB.toString();
    }

    /*
     * verifySignUsingCrypto verifies the spark message
     * @param stringToVerify, the string that will be used to verify the spark message
     * @param xVaultAPISignature, the signature of the message
     * @param pubKey, the public key used to verify the message
     * @returns verified, a boolean value indicating whether the spark message was valid
     */
    public static boolean verifySignUsingCrypto(String stringToVerify, String xVaultAPISignature, PublicKey pubKey) {
        final Logger logger = LogManager.getLogger();
        boolean verified = false;
        try {
            // Create signature using the public key and the string to verify
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(pubKey);
            signature.update(stringToVerify.getBytes());
            // Verify that the string to verify matches the signature received in the headers
            verified = signature.verify(Base64.getMimeDecoder().decode(xVaultAPISignature.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            logger.error(e.getMessage());
        }
        return verified;
    }
}
