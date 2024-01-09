package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "VaultConfigurationRecordCache")
public class VaultConfigurationRecordCache extends VaultConfigurationRecord{

    @Column(name="objectName")
    private String objectName;

    @Column(name = "cachedTime", columnDefinition = "TIMESTAMP")
    private LocalDateTime cachedTime;

    @Column(name = "field")
    private String field;

    public VaultConfigurationRecordCache(){
        super();
    }

    public VaultConfigurationRecordCache(String objectName, String id, String globalId, String field, String definition, LocalDateTime cachedTime){
        super();
        setId(id);
        setGlobalId(globalId);
        setDefinition(definition);
        this.cachedTime = cachedTime;
        this.objectName = objectName;
    }

    public LocalDateTime getCachedTime() {
        return cachedTime;
    }

    public void setCachedTime(LocalDateTime cachedTime) {
        this.cachedTime = cachedTime;
    }
}
