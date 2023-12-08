package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Session {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public long id;

    @JsonProperty("session_id")
    private String sessionId;

    public Session(){

    }

    public Session(String sessionId){
        this.sessionId = sessionId;
    }

    public String getSessionId(){
        return this.sessionId;
    }
}
