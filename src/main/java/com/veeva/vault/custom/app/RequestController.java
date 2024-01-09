package com.veeva.vault.custom.app;

import com.veeva.vault.custom.app.admin.*;
import com.veeva.vault.custom.app.client.*;
import com.veeva.vault.custom.app.model.json.*;
import com.veeva.vault.custom.app.model.http.*;

import com.veeva.vault.vapil.api.client.VaultClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(path = "/api/")
public class RequestController {
    @Autowired
    RequestUtilities requestUtilities;
    ArrayBlockingQueue<SparkMessageRequest> sparkMessageQueue = new ArrayBlockingQueue<>(20000);

    @PostMapping(path = "/rest/{environment}/{customerId}/{endPoint}", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity handleFormPost(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestParam MultiValueMap<String,String> paramMap, HttpServletRequest request, HttpServletResponse response){
        if(requestUtilities.throttleRequest(request)) {
            Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.base__v, "POST", request);
            ResponseEntity responseEntity;
            if (entry != null) {
                Processor processor = entry.getValue();
                Integer statusCode = entry.getKey();
                if (processor != null && statusCode != null && statusCode == 200) {
                    requestUtilities.initiateRequest(processor.getId());
                    HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), paramMap.toSingleValueMap(), null, request.getRemoteAddr(), request.getRemoteHost());
                    responseEntity = requestUtilities.processRestRequest(httpRequest, processor);
                } else if (processor != null && statusCode != null && statusCode != 200) {
                    responseEntity = ResponseEntity.status(statusCode).build();
                } else {
                    responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
                }
            } else {
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
            requestUtilities.closeRequest();
            return responseEntity;
        }else{
            return ResponseEntity.status(429).body("You have exceeded rate limits. Please try again later");
        }
    }

    @PostMapping(path = "/rest/{environment}/{customerId}/{endPoint}", consumes = {"application/json", "application/xml"})
    public ResponseEntity handleFormPost(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestParam MultiValueMap<String,String> paramMap, @RequestBody String body, HttpServletRequest request, HttpServletResponse response){
        if(requestUtilities.throttleRequest(request)) {
            Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.base__v, "POST", request);
            ResponseEntity responseEntity;
            if (entry != null) {
                Processor processor = entry.getValue();
                Integer statusCode = entry.getKey();
                if (processor != null && statusCode != null && statusCode == 200) {
                    requestUtilities.initiateRequest(processor.getId());
                    HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), paramMap.toSingleValueMap(), body, request.getRemoteAddr(), request.getRemoteHost());
                    responseEntity = requestUtilities.processRestRequest(httpRequest, processor);
                } else if (processor != null && statusCode != null && statusCode != 200) {
                    responseEntity = ResponseEntity.status(statusCode).build();
                } else {
                    responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
                }
            } else {
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
            requestUtilities.closeRequest();
            return responseEntity;
        }else{
            return ResponseEntity.status(429).body("You have exceeded rate limits. Please try again later");
        }
    }

    @GetMapping(path = "/rest/{environment}/{customerId}/{endPoint}")
    public ResponseEntity handleGet(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestParam MultiValueMap<String,String> paramMap, HttpServletRequest request, HttpServletResponse response){
        if(requestUtilities.throttleRequest(request)) {
            Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.base__v, "GET", request);
            ResponseEntity responseEntity;
            if (entry != null) {
                Processor processor = entry.getValue();
                Integer statusCode = entry.getKey();
                if (processor != null && statusCode != null && statusCode == 200) {
                    requestUtilities.initiateRequest(processor.getId());
                    HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), paramMap.toSingleValueMap(), null, request.getRemoteAddr(), request.getRemoteHost());
                    responseEntity = requestUtilities.processRestRequest(httpRequest, processor);
                } else if (processor != null && statusCode != null && statusCode != 200) {
                    responseEntity = ResponseEntity.status(statusCode).build();
                } else {
                    responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
                }
            } else {
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
            requestUtilities.closeRequest();
            return responseEntity;
        }else{
            return ResponseEntity.status(429).body("You have exceeded rate limits. Please try again later");
        }
    }

    @PostMapping(path = "/spark/{environment}/{customerId}/{endPoint}", produces = "application/json")
    public ResponseEntity handleSparkMessage(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestBody(required=true) String body, HttpServletRequest request, HttpServletResponse response){
        Map<String,String> headers = httpHeaders.toSingleValueMap();
        String certificateId = headers.get("X-VaultAPISignature-CertificateId")!=null? headers.get("X-VaultAPISignature-CertificateId") : headers.get("x-vaultapisignature-certificateid");
        JsonObject jsonBody = new JsonObject(body);
        String vaultName = jsonBody.getString("vault_host_name");;
        String sessionId = requestUtilities.getSessionId(jsonBody);
        String certificateFile = vaultName!=null && sessionId!=null? requestUtilities.getPublicKey(vaultName, VaultClient.VAULT_API_VERSION, sessionId, certificateId) : null;
        boolean isValidMessage = certificateFile!=null? requestUtilities.validateSpark(headers, body, certificateFile) : false;
        if(isValidMessage && certificateFile!=null && sessionId!=null && vaultName!=null) {
            Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.spark__c, "POST", request);
            if(entry!=null) {
                Processor processor = entry.getValue();
                Integer statusCode = entry.getKey();
                if (processor != null && statusCode != null && statusCode == 200) {
                    this.sparkMessageQueue.add(new SparkMessageRequest(new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), new HashMap<String, String>(), body, request.getRemoteAddr(), request.getRemoteHost()), processor));
                    return ResponseEntity.ok("SUCCESS");
                }else if(processor !=null && statusCode != null && statusCode != 200){
                    return ResponseEntity.status(statusCode).build();
                } else{
                    return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
                }
            }else{
                return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        } else {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }
    }

    @GetMapping(path = "/job/{environment}/{customerId}/{endPoint}")
    public ModelAndView getJob(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response, Model model) {
        Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.web_job__c, "POST", request);
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                model.addAttribute("environment", environment);
                model.addAttribute("customerId", customerId);
                model.addAttribute("endpoint", endPoint);
                return new ModelAndView("jobPost");
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }

    @GetMapping(path = "/webaction/{environment}/{customerId}/{endPoint}")
    public ModelAndView getWebAction(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response, Model model) {
        Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.web_action__c, "POST", request);
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                model.addAttribute("environment", environment);
                model.addAttribute("customerId", customerId);
                model.addAttribute("endpoint", endPoint);
                return new ModelAndView("webActionPost");
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }

    @PostMapping(path = "/job/{environment}/{customerId}/{endPoint}")
    public ResponseEntity<String> postJob(@RequestParam Map<String,String> requestBody, @PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response) {
        Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.web_job__c, "POST", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                requestUtilities.initiateRequest(processor.getId());
                httpHeaders.add("authorization", requestBody.get("Session.id"));
                httpHeaders.add("x-vault-dns", requestBody.get("vaultDNS"));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), requestBody, null, request.getRemoteAddr(), request.getRemoteHost());
                responseEntity = requestUtilities.processJobRequest(httpRequest, processor);
            }else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        requestUtilities.closeRequest();
        return responseEntity;
    }

    @PostMapping(path = "/webaction/{environment}/{customerId}/{endPoint}")
    public ResponseEntity<String> postWebAction(@RequestParam Map<String,String> requestBody, @PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response) {
        Map.Entry<Integer, Processor> entry = requestUtilities.validateRequest(environment, customerId, endPoint, Processor.Type.web_action__c, "POST", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                requestUtilities.initiateRequest(processor.getId());
                httpHeaders.add("authorization", requestBody.get("Session.id"));
                httpHeaders.add("x-vault-dns", requestBody.get("vaultDNS"));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), requestBody, null, request.getRemoteAddr(), request.getRemoteHost());
                responseEntity = requestUtilities.processWebActionRequest(httpRequest, processor);
            }else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        requestUtilities.closeRequest();
        return responseEntity;
    }

    @Scheduled(fixedRateString = "100")
    public void pollMessageQueue() {
        final Logger logger = Logger.getLogger(this.getClass());
        SparkMessageRequest sparkRequest = null;
        try {
            sparkRequest = sparkMessageQueue.poll(50, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (sparkRequest != null) {
            requestUtilities.processSparkRequest(sparkRequest);
        }
    }
}
