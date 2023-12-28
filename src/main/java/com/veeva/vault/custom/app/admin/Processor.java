package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Processor {
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

    public enum Environment{
        sandbox__c,
        validation__c,
        production__c,
        prerelease__c;

        public String toLabel(){
            String environmentType = null;
            switch(this){
                case sandbox__c:
                    environmentType="Sandbox";
                    break;
                case validation__c:
                    environmentType="Validation";
                    break;
                case production__c:
                    environmentType="Production";
                    break;
                case prerelease__c:
                    environmentType="Prerelease";
                    break;
            }
            return environmentType;
        }
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
    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("log_level__c")
    private List<LogLevel> logLevel;

    @JsonProperty("object_type__vr.api_name__v")
    private Type processorType;

    @JsonProperty("environment_type__c")
    private List<Environment> environmentType;

    @JsonProperty("customer__cr.name__v")
    private String customerName;

    @JsonProperty("customer__cr.api_name__c")
    private String customerApiName;

    @JsonProperty("endpoint_url__c")
    private String endpointUrl;

    @JsonProperty("authentication_method__c")
    private List<AuthenticationMethod> authenticationMethod;

    @JsonProperty("definition__c")
    @Column(length = 32000)
    private String definition;

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
    }

    public Processor(String customerId, String endPointUrl, Processor.Type type){
        this.customerApiName = customerId;
        this.endpointUrl = endPointUrl;
        this.processorType = type;
    }

    public String getId(){
        return id;
    }

    public LogLevel getLogLevel() {
        return logLevel!=null && !logLevel.isEmpty()? logLevel.get(0) : null;
    }

    public Type getProcessorType() {
        return processorType;
    }

    public Environment getEnvironmentType() {
        return environmentType!=null && !environmentType.isEmpty()? environmentType.get(0) : null;
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

    public String getDefinition() {
        return definition;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Embeddable
    public static class ScriptLibraryHolder{

        @JsonProperty("data")
        @JdbcTypeCode(SqlTypes.JSON)
        private List<ScriptLibrary> scriptLibraries;

        public ScriptLibraryHolder(){

        }

        public List<ScriptLibrary> getScriptLibraries() {
            return scriptLibraries;
        }
    }
}
