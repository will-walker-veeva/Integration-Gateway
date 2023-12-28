package com.veeva.vault.custom.app.admin;

import com.veeva.vault.custom.app.model.json.JsonObject;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
public class CacheContext {
    @Id
    private String vaultDns;

    private Instant lastAccessTime;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> data = new HashMap<String, Object>();

    public CacheContext(String vaultDns){
        this.vaultDns = vaultDns;
    }

    public void put(String key, Object value){
        lastAccessTime = Instant.now();
        this.data.put(key, value);
    }

    public Object get(String key){
        lastAccessTime = Instant.now();
        return this.data.get(key);
    }

    public Instant getLastAccessTime(){
        return this.lastAccessTime;
    }
}
