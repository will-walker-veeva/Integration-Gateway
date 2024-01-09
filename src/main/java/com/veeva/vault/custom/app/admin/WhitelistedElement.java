package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "WhitelistedElement")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhitelistedElement {
    public static final String OBJECT_NAME = "whitelisted__c";

    public enum Type{
        whitelisted_domain__c,
        whitelisted_ip_range__c
    }

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("start_ip_range__c")
    private String startIpRange;

    @JsonProperty("end_ip_range__c")
    private String endIpRange;

    @JsonProperty("object_type__vr.api_name__v")
    private Type whitelistType;

    @JsonProperty("processor__c")
    @Column(name = "processorId")
    private String processorId;

    @JsonProperty("domain_name__c")
    private String domainName;

    public WhitelistedElement(){

    }

    public String getStartIpRange() {
        return startIpRange;
    }

    public void setStartIpRange(String startIpRange) {
        this.startIpRange = startIpRange;
    }

    public String getEndIpRange() {
        return endIpRange;
    }

    public void setEndIpRange(String endIpRange) {
        this.endIpRange = endIpRange;
    }

    public Type getWhitelistType() {
        return whitelistType;
    }

    public void setWhitelistType(Type whitelistType) {
        this.whitelistType = whitelistType;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
