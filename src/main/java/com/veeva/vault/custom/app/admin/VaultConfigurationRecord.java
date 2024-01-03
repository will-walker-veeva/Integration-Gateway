package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


@MappedSuperclass
public abstract class VaultConfigurationRecord implements Serializable {
    @Id
    @JsonProperty("id")
    public String id;

    @JsonProperty("global_id__sys")
    public String globalId;

    @JsonProperty("definition__c")
    @JsonAlias({ "script_library__cr.definition__c" , "referring_library__cr.definition__c"})
    @Column(length = 32000)
    private String definition;

    private EnvironmentType environmentType;

    public final EnvironmentType getEnvironmentType() {
        return environmentType;
    }
    @JsonSetter("environment_type__c")
    public void setEnvironmentType(List<String> environmentType){
        this.environmentType = EnvironmentType.forStringList(environmentType);
    }
    @JsonGetter("environment_type__c")
    public final List<EnvironmentType> getEnvironmentTypes(){
        if(this.environmentType!=null) return Arrays.asList(this.environmentType);
        else return null;
    }

    public final String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public final String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public final String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
