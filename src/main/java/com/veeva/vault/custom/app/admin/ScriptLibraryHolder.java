package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Embeddable
public class ScriptLibraryHolder {
    @JsonProperty("data")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ScriptLibrary> scriptLibraries;

    public ScriptLibraryHolder(){

    }

    public List<ScriptLibrary> getScriptLibraries() {
        return scriptLibraries;
    }
}
