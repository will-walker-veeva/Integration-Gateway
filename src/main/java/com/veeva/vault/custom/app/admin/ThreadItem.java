package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreadItem {
    @Id
    private String threadId;

    @JsonProperty("processor_id")
    private String processorId;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> temporaryTables = new ArrayList<>();

    public ThreadItem(){

    }

    public ThreadItem(String threadId, String processorId){
        this.threadId = threadId;
        this.processorId = processorId;
        this.temporaryTables = new ArrayList<>();
    }

    public String getThreadId() {
        return threadId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public List<String> getTemporaryTables(){
        return this.temporaryTables;
    }

    public void addTemporaryTable(String temporaryTable){
        this.temporaryTables.add(temporaryTable);
    }

    public void clearTemporaryTables(){
        this.temporaryTables.clear();
    }
}
