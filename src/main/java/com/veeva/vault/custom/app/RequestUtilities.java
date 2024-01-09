package com.veeva.vault.custom.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.admin.*;
import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.FilesClient;
import com.veeva.vault.custom.app.client.HttpClient;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;
import com.veeva.vault.custom.app.model.http.HttpResponseType;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.repository.IPWhitelistRepository;
import com.veeva.vault.custom.app.repository.ThreadRegistry;
import com.veeva.vault.custom.app.repository.VaultProcessorRepository;
import com.veeva.vault.custom.app.repository.VaultSessionRepository;
import com.veeva.vault.vapil.api.client.VaultClient;
import com.veeva.vault.vapil.api.model.response.ObjectRecordBulkResponse;
import com.veeva.vault.vapil.api.request.ObjectRecordRequest;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.veeva.vault.custom.app.client.Client.VAULT_CLIENT_ID;

@Service
public class RequestUtilities {
    @Autowired
    ScriptUtilities scriptExecutionUtils;
    @Autowired
    VaultProcessorRepository configurationRepository;

    @Autowired
    VaultSessionRepository sessionRepository;

    @Autowired
    IPWhitelistRepository whitelistRepository;

    @Autowired
    AppConfiguration appConfiguration;
    @Autowired
    ThreadRegistry threadRegistry;

    private static final String APP_KEY = java.util.UUID.randomUUID().toString();

    public void initiateRequest(String processorId){
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processorId));
    }

    public void closeRequest(){
        threadRegistry.delete(new ThreadItem(Thread.currentThread().getName(), null));
    }

    public Map.Entry<Integer, Processor> validateRequest(String environment, String customerId, String endpoint, Processor.Type processorType, String requestMethod, HttpServletRequest request){
        String environmentType = environment.toLowerCase()+"__c";
        Processor exampleProcessor = new Processor(customerId, endpoint, processorType, EnvironmentType.valueOf(environmentType));
        String method = requestMethod.toLowerCase()+"__c";
        Collection<Processor> processors = (Collection<Processor>) this.configurationRepository.findAll(Example.of(exampleProcessor));
        if(processors!=null && !processors.isEmpty()){
            Processor processor = processors.stream().filter(item -> item.getMethod() == Processor.Method.valueOf(method)).findFirst().orElse(null);
            if(processor!=null) {
                String userIp = request.getRemoteAddr();
                Collection<WhitelistedElement> whitelistedElements = whitelistRepository.findAllByProcessor(processor.getId());
                if(whitelistedElements.stream().filter(el -> el.getWhitelistType() == WhitelistedElement.Type.whitelisted_ip_range__c).noneMatch(el -> checkIPv4IsInRangeByConvertingToInt(userIp, el.getStartIpRange(), el.getEndIpRange())) && whitelistedElements.stream().filter(el -> el.getWhitelistType() == WhitelistedElement.Type.whitelisted_domain__c).noneMatch(el -> checkDomain(userIp, el.getDomainName()))){
                    return new AbstractMap.SimpleEntry(401, processor);
                }
                String authorizationToken = request.getHeader("Authorization");
                if (processorType == Processor.Type.base__v && authorizationToken == null) {
                    return new AbstractMap.SimpleEntry(401, processor);
                } else if (processorType == Processor.Type.base__v && authorizationToken != null && processor.getAuthenticationMethod()!=null && processor.getAuthenticationMethod() == Processor.AuthenticationMethod.api_token__c) {
                    if(authorizationToken.equals(processor.getApiToken())||authorizationToken.equals("Bearer "+processor.getApiToken())) {
                        return new AbstractMap.SimpleEntry(200, processor);
                    }else{
                        return new AbstractMap.SimpleEntry(401, processor);
                    }
                }else{
                    return new AbstractMap.SimpleEntry(200, processor);
                }
            }else{
                return null;
            }
        }
        return null;
    }
    public void closeClient(String processorId, Client client, com.veeva.vault.custom.app.client.Logger.Level levelToLog){
        List<Log> logs = client.getLogs();
        if(logs!=null && !logs.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                objectMapper.registerModule(new JavaTimeModule());
                if(sessionRepository.count()>0) {
                    List<Log> publishableLogs = logs.stream().filter(each -> filter(each, levelToLog)).map(each -> {
                        each.setProcessorId(processorId);
                        return each;
                    }).sorted(Comparator.comparing(Log::getCreated).reversed()).collect(Collectors.toList());
                    Session session = sessionRepository.findAll().iterator().next();
                    VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(session.getSessionId()).build();
                    if(publishableLogs.size()<=2000) {
                        for (int i = 0; i <= publishableLogs.size() / 500; i++) {
                            List<Log> logBatch = publishableLogs.subList(i * 500, Math.min((i + 1) * 500, publishableLogs.size()));
                            ObjectRecordRequest logCreationRequest = vaultClient.newRequest(ObjectRecordRequest.class);
                            logCreationRequest.setContentTypeJson();
                            logCreationRequest.setRequestString(objectMapper.writeValueAsString(logBatch));
                            ObjectRecordBulkResponse response = logCreationRequest.createObjectRecords("log__c");
                        }
                    }else{
                        publishableLogs = Arrays.asList(new Log(processorId, Logger.Level.ERROR.getAPIName(), "Number of logs exceeded 2000 and have not been recorded, consider reducing the logging scope", Logger.DATE_TIME_FORMATTER.format(Instant.now())));
                        ObjectRecordRequest logCreationRequest = vaultClient.newRequest(ObjectRecordRequest.class);
                        logCreationRequest.setContentTypeJson();
                        logCreationRequest.setRequestString(objectMapper.writeValueAsString(publishableLogs));
                        ObjectRecordBulkResponse response = logCreationRequest.createObjectRecords("log__c");
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        client.deleteAllFiles();
    }

    private boolean filter(Log log, Logger.Level level){
        switch(level){
            case INFO:
                return log.getLevel()!= Logger.Level.DEBUG && log.getLevel()!=Logger.Level.TRACE;
            case WARN:
                return log.getLevel()==Logger.Level.WARN;
            case DEBUG:
                return log.getLevel()!=Logger.Level.TRACE;
            case ERROR:
                return log.getLevel()== Logger.Level.ERROR || log.getLevel()==Logger.Level.WARN;
            case TRACE:
                return true;
        }
        return true;
    }

    public ResponseEntity processRestRequest(HttpRequest httpRequest, Processor processor){
        ResponseEntity responseEntity = null;
        HttpResponse httpResponse = new HttpResponse();
        Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
        if (client != null && httpResponse.getResponseCode() == 200) {
            if (processor.getResponseType() == Processor.ResponseType.file__c) {
                try {
                    responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(new FilesClient().readFileToByteArray(httpResponse.getResponseFile()));
                } catch (Exception e) {
                    responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
                }
            } else if (processor.getResponseType() == Processor.ResponseType.webpage__c) {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseView());
            } else {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
            }
            closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
        } else if (client != null && httpResponse.getResponseCode() != 200) {
            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
            closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
        } else {
            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
        }
        return responseEntity;
    }

    public ResponseEntity processWebActionRequest(HttpRequest httpRequest, Processor processor){
        ResponseEntity responseEntity = null;
        try {
            HttpResponse httpResponse = new HttpResponse();
            Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
            if (client != null && httpResponse.getResponseCode() == 200) {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseView());
                closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
            } else if (client != null && httpResponse.getResponseCode() != 200) {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
            } else {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
            }
        } catch (Exception e) {
            responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
        }
        return responseEntity;
    }

    public ResponseEntity processJobRequest(HttpRequest httpRequest, Processor processor){
        ResponseEntity responseEntity = null;
        try {
            HttpResponse httpResponse = new HttpResponse();
            Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
            if (client != null && httpResponse.getResponseCode() == 200) {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body("SUCCESS");
                closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
            } else if (client != null && httpResponse.getResponseCode() != 200) {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
            } else {
                responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
            }
        } catch (Exception e) {
            responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
        }
        return responseEntity;
    }

    public void processSparkRequest(SparkMessageRequest sparkRequest){
        Processor processor = sparkRequest.getProcessor();
        HttpRequest httpRequest = sparkRequest.getHttpRequest();
        initiateRequest(processor.getId());
        Client client = null;
        try {
            HttpResponse httpResponse = new HttpResponse();
            client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);

        } catch (Exception e) {
            Logger.getLogger(processor.getId()).error(e.getMessage(), e);
        }finally{
            if (client != null) {
                closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
            }
        }
        closeRequest();
    }

    public boolean checkIPv4IsInRangeByConvertingToInt (String inputIP, String rangeStartIP, String rangeEndIP)  {
        try{
            long startIPAddress = ipToLongInt(InetAddress.getByName(rangeStartIP));
            long endIPAddress = ipToLongInt(InetAddress.getByName(rangeEndIP));
            long inputIPAddress = ipToLongInt(InetAddress.getByName(inputIP));
            return (inputIPAddress >= startIPAddress && inputIPAddress <= endIPAddress);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkDomain(String inputIP, String domain){
        try{
            String inputDomain = InetAddress.getByName(inputIP).getHostName();
            String elDomain = InetAddress.getByName(domain).getHostName();
            return inputDomain.equals(elDomain);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public long ipToLongInt (InetAddress ipAddress) {
        long resultIP = 0;
        byte[] ipAddressOctets = ipAddress.getAddress();

        for (byte octet : ipAddressOctets) {
            resultIP <<= 8;
            resultIP |= octet & 0xFF;
        }
        return resultIP;
    }

    /*
     * getPublicKey makes a REST API call to the Vault HTTPS REST API to retrieve the certificate/pem file
     * @param sessionId, an active sessionId for the vault
     * @param certificateId, the id of the certificate/pem to be retrieved
     * @param hostname, the vault hostname
     * @returns certificate, the certificate retrieved from the vault
     */
    public String getPublicKey(String hostname, String apiVersion, String sessionId, String certificateId)  {
        final Logger logger = Logger.getLogger(RequestUtilities.class);
        try{
            byte[] publicKey = getCertificate(hostname, apiVersion, sessionId, certificateId);
            return new String(publicKey);
        }catch(Exception e){

        }
        return null;
    }

    public byte[] getCertificate(String domain, String apiVersion, String sessionId, String certificateId) throws Exception {
        String url = String.format("https://%s/api/%s/services/certificate/%s", domain, apiVersion, certificateId);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("X-VaultAPI-ClientID", VAULT_CLIENT_ID);
        headers.put("Authorization", sessionId);
        byte[] response = HttpClient.newInstance().get(url, headers, HttpResponseType.BINARY);
        return response;
    }

    public boolean validateSpark(Map<String, String> headers, String body, String certificateFile){
        String pemKey = certificateFile
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("\\R", "")
                .replace("-----END CERTIFICATE-----", "");

        // Decodes key from base 64
        byte[] decoded = Base64.getMimeDecoder().decode(pemKey.getBytes());
        InputStream certStream = new ByteArrayInputStream(decoded);
        PublicKey publicKey = null;
        try {
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(certStream);
            publicKey = certificate.getPublicKey();
        } catch (CertificateException e) {

        }
        return validateSpark(headers, body, publicKey);
    }

    /*
     * validates the message sent to the lambda function using the public key
     * @param headers, the map containing the headers received
     * @param body, the body received as a json string
     * @param publicKey, the public key used to verify the message
     * @returns verified, an boolean representing whether the spark message was valid
     */
    public boolean validateSpark(Map<String,String> headers, String body, PublicKey publicKey) {
        final Logger logger = Logger.getLogger(RequestUtilities.class);
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
    public String prepareDataToVerify(Map<String,String> headers, String body) {
        final Logger logger = Logger.getLogger(RequestUtilities.class);
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
    public boolean verifySignUsingCrypto(String stringToVerify, String xVaultAPISignature, PublicKey pubKey) {
        final Logger logger = Logger.getLogger(RequestUtilities.class);
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

    public String getSessionId(JsonObject jsonBody){
        JsonObject message = jsonBody.getJsonObject("message");
        JsonObject attributes = message != null ? message.getJsonObject("attributes") : null;
        String sessionId = attributes != null ? attributes.getString("authorization") : null;
        return sessionId;
    }

    public Bucket createNewBucket() {
        long capacity = 10;
        Refill refill = Refill.greedy(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean throttleRequest(HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(true);
        Bucket bucket = (Bucket) session.getAttribute("throttler-" +   APP_KEY);
        if (bucket == null) {
            bucket = createNewBucket();
            session.setAttribute("throttler-" + APP_KEY, bucket);
        }
        boolean okToGo = bucket.tryConsume(1);
        return okToGo;
    }
}
