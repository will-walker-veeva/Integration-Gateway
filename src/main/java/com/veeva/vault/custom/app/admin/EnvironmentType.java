package com.veeva.vault.custom.app.admin;


import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public enum EnvironmentType {
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

    @JsonCreator
    public static EnvironmentType forString(String value){
        return EnvironmentType.valueOf(value);
    }

    @JsonCreator
    public static EnvironmentType forStringList(List<String> value){
        return value!=null && !value.isEmpty()? EnvironmentType.valueOf(value.get(0)) : null;
    }
}
