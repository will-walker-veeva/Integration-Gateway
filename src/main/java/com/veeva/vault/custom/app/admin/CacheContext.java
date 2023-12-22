package com.veeva.vault.custom.app.admin;

import com.veeva.vault.custom.app.model.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class CacheContext {
    @Id
    private String vaultDns;

    private Instant lastAccessTime;

    private JsonObject data = new JsonObject();

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
