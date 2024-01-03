package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Embeddable
@Entity
public class ScriptLibrary extends VaultConfigurationRecord {
    public static final String OBJECT_NAME = "script_library__c";

    @JsonProperty("package_name__c")
    @JsonAlias({ "script_library__cr.package_name__c" , "referring_library__cr.package_name__c"})
    private String packageName;

    @JsonProperty("class_name__c")
    @JsonAlias({ "script_library__cr.class_name__c", "referring_library__cr.class_name__c" })
    private String className;

    @JsonProperty("validated_name__c")
    @JsonAlias({ "script_library__cr.validated_name__c", "referring_library__cr.validated_name__c"})
    private String validatedName;

    @JsonProperty("childibrary_joins__cr")
    @JdbcTypeCode(SqlTypes.JSON)
    private ScriptLibraryHolder scriptLibraryHolder;

    public ScriptLibrary() {
    }

    @JsonIgnore
    public String getName(){
        return this.packageName+"."+this.className;
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
    public ScriptLibraryHolder getScriptLibraryHolder() {
        return scriptLibraryHolder;
    }
}
