package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Arrays;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Processor extends VaultConfigurationRecord {
    public static final String OBJECT_NAME = "processor__c";
    public enum LogLevel{
        error__c,
        warn__c,
        info__c,
        debug__c,
        trace__c
    }

    public enum Type{
        spark__c,
        web_job__c,
        web_action__c,
        base__v
    }

    public enum AuthenticationMethod{
        api_token__c,
        session_id__c
    }

    public enum ResponseType{
        json_object__c,
        file__c,
        webpage__c
    }

    public enum Method{
        get__c,
        post__c,
        put__c,
        delete__c
    }

    @JsonProperty("log_level__c")
    private List<LogLevel> logLevel;

    @JsonProperty("object_type__vr.api_name__v")
    private Type processorType;

    @JsonProperty("customer__cr.name__v")
    private String customerName;

    @JsonProperty("customer__cr.api_name__c")
    private String customerApiName;

    @JsonProperty("endpoint_url__c")
    private String endpointUrl;

    @JsonProperty("authentication_method__c")
    private List<AuthenticationMethod> authenticationMethod;

    @JsonProperty("configuration__c")
    @Column(length = 32000)
    private String configuration;

    @JsonProperty("api_token__c")
    private String apiToken;

    @JsonProperty("response_type__c")
    private List<ResponseType> responseType;

    @JsonProperty("method__c")
    private List<Method> method;

    @JsonProperty("libraryprocessor_joins__cr")
    @JdbcTypeCode(SqlTypes.JSON)
    private ScriptLibraryHolder scriptLibraryHolder;

    public Processor() {
        super();
    }

    public Processor(String customerId, String endPointUrl, Processor.Type type, EnvironmentType environmentType){
        super();
        this.customerApiName = customerId;
        this.endpointUrl = endPointUrl;
        this.processorType = type;
        super.setEnvironmentType(Arrays.asList(environmentType.toString()));
    }

    public LogLevel getLogLevel() {
        return logLevel!=null && !logLevel.isEmpty()? logLevel.get(0) : null;
    }

    public Type getProcessorType() {
        return processorType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerApiName() {
        return customerApiName;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod!=null && !authenticationMethod.isEmpty()? authenticationMethod.get(0) : null;
    }

    public String getApiToken() {
        return apiToken;
    }

    public ResponseType getResponseType() {
        return responseType!=null && !responseType.isEmpty()? responseType.get(0) : null;
    }

    public String getConfiguration() {
        return configuration;
    }

    public Method getMethod() {
        return method!=null && !method.isEmpty()? method.get(0) : null;
    }

    public ScriptLibraryHolder getScriptLibraryHolder() {
        return scriptLibraryHolder;
    }

}
