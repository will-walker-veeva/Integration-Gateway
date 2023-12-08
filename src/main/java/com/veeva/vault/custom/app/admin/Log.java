package com.veeva.vault.custom.app.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.veeva.vault.custom.app.client.Logger;

public class Log {
    @JsonProperty("message__c")
    private String message;
    @JsonProperty("stack_trace__c")
    private String stackTrace;
    @JsonProperty("created__c")
    private String created;
    @JsonProperty("log_level__c")
    private String levelAPIName;
    @JsonProperty("thread_id__c")
    private String threadId;
    @JsonProperty("processor__c")
    private String processorId;

    private Logger.Level level;

    public Log() {
    }

    public Log(String processorId, String levelAPIName, String message, String created) {
        this.processorId = processorId;
        this.message = message;
        this.created = created;
        this.levelAPIName = levelAPIName;
        this.threadId = Thread.currentThread().getName();
        this.level = Logger.Level.ofAPIName(levelAPIName);
    }

    public Log(String processorId, String levelAPIName, String message, String stackTrace, String created) {
        this.processorId = processorId;
        this.message = message;
        this.stackTrace = stackTrace;
        this.created = created;
        this.levelAPIName = levelAPIName;
        this.threadId = Thread.currentThread().getName();
        this.level = Logger.Level.ofAPIName(levelAPIName);
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getCreated() {
        return created;
    }

    public String getLevelAPIName() {
        return levelAPIName;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public Logger.Level getLevel() {
        return level;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }
}
