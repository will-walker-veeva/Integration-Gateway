package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Embeddable
public class ScriptLibrary implements Serializable {
    public static final String OBJECT_NAME = "script_library__c";
    @JsonProperty("definition__c")
    @JsonAlias({ "script_library__cr.definition__c" , "referring_library__cr.definition__c"})
    @Column(length = 32000)
    private String definition;

    @JsonProperty("package_name__c")
    @JsonAlias({ "script_library__cr.package_name__c" , "referring_library__cr.package_name__c"})
    private String packageName;

    @JsonProperty("class_name__c")
    @JsonAlias({ "script_library__cr.class_name__c", "referring_library__cr.class_name__c" })
    private String className;

    @JsonProperty("validated_name__c")
    @JsonAlias({ "script_library__cr.validated_name__c", "referring_library__cr.validated_name__c"})
    private String validatedName;

    public ScriptLibrary() {
    }

    public String getDefinition() {
        return definition;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getValidatedName() {
        return validatedName;
    }
}
