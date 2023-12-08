package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreadItem {
    @Id
    private String threadId;

    @JsonProperty("processor_id")
    private String processorId;

    public ThreadItem(){

    }

    public ThreadItem(String threadId, String processorId){
        this.threadId = threadId;
        this.processorId = processorId;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getProcessorId() {
        return processorId;
    }
}
