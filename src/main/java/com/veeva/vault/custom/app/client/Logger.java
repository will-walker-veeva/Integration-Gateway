package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logger {
    private static final Map<String, List<Log>> logs = new HashMap<String, List<Log>>();
    private org.apache.logging.log4j.Logger logger;
    private String requestProcessorId;
    private Logger(){
        this.logger = LogManager.getLogger();
    }

    private Logger(String requestProcessorId){
        this.logger = LogManager.getLogger();
        this.requestProcessorId = requestProcessorId;
    }

    public static List<Log> getLogs(String threadId){
        List<Log> logsToSend = logs.get(threadId);
        logs.put(threadId, new ArrayList<Log>());
        return logsToSend;
    }

    private Logger(Class className){
        this.logger = LogManager.getLogger(className);
    }

    public static Logger getLogger(Class className){
        return new Logger(className);
    }

    public static Logger getLogger(String requestProcessorId){
        return new Logger(requestProcessorId);
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneId.ofOffset("", ZoneOffset.UTC));
    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR;

        public String getAPIName(){
            return this.toString().toLowerCase() + "__c";
        }
        public static Logger.Level ofAPIName(String apiName){
            return Level.valueOf(apiName.replace("__c", "").toUpperCase());
        }
    }

    public void log(String message, Level level) {
        String threadId = Thread.currentThread().getName();
        if(logs.get(threadId) == null) logs.put(threadId, new ArrayList<Log>());
        logs.get(threadId).add(new Log(this.requestProcessorId, level.getAPIName(), message, formatter.format(Instant.now())));
        logger.log(org.apache.logging.log4j.Level.valueOf(level.toString()), message);
    }


    public void log(String message, Level level, Object... arguments) {
        Message logMessage = new ParameterizedMessage(message, arguments);
        String formattedMessage = logMessage.getFormattedMessage();
        String threadId = Thread.currentThread().getName();
        if(logs.get(threadId) == null) logs.put(threadId, new ArrayList<Log>());
        logs.get(threadId).add(new Log(this.requestProcessorId, level.getAPIName(), formattedMessage, formatter.format(Instant.now())));
        logger.log(org.apache.logging.log4j.Level.valueOf(level.toString()), message);;
        logger.log(org.apache.logging.log4j.Level
                .valueOf(level.toString()), logMessage);
    }

    public void log(String message, String stackTrace, Level level) {
        String threadId = Thread.currentThread().getName();
        if(logs.get(threadId) == null) logs.put(threadId, new ArrayList<Log>());
        logs.get(threadId).add(new Log(this.requestProcessorId, level.getAPIName(), message, stackTrace, formatter.format(Instant.now())));
        logger.log(org.apache.logging.log4j.Level
                .valueOf(level.toString()), message, stackTrace);
    }

    public void log(String message, Level level, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String threadId = Thread.currentThread().getName();
        if(logs.get(threadId) == null) logs.put(threadId, new ArrayList<Log>());
        logs.get(threadId).add(new Log(this.requestProcessorId, level.getAPIName(), message, sw.toString(), formatter.format(Instant.now())));
        logger.log(org.apache.logging.log4j.Level
                .valueOf(level.toString()), message, e.getStackTrace());
    }

    public void log(String message, Level level,  Throwable e, Object ... arguments) {
        Message logMessage = new ParameterizedMessage(message, arguments);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String formattedMessage = logMessage.getFormattedMessage();
        String threadId = Thread.currentThread().getName();
        if(logs.get(threadId) == null) logs.put(threadId, new ArrayList<Log>());
        logs.get(threadId).add(new Log(this.requestProcessorId, level.getAPIName(), formattedMessage, sw.toString(), formatter.format(Instant.now())));
        logger.log(org.apache.logging.log4j.Level
                .valueOf(level.toString()), logMessage, e);
    }
    
    public void error(String message){
        log(message, Level.ERROR);
    }
    public void error(String message, Object... arguments){
        log(message, Level.ERROR, arguments);
    }
    public void error(String message, String stackTrace){
        log(message, Level.ERROR, stackTrace);
    }
    public void error(String message, Throwable e){
        log(message, Level.ERROR, e);
    }
    public void error(String message, Throwable e, Object ... arguments){
        log(message, Level.ERROR, e, arguments);
    }
    public void warn(String message){
        log(message, Level.WARN);
    }
    public void warn(String message, Object... arguments){
        log(message, Level.WARN, arguments);
    }
    public void warn(String message, String stackTrace){
        log(message, Level.WARN, stackTrace);
    }
    public void warn(String message, Throwable e){
        log(message, Level.WARN, e);
    }
    public void warn(String message, Throwable e, Object ... arguments){
        log(message, Level.WARN, e, arguments);
    }
    public void info(String message){
        log(message, Level.INFO);
    }
    public void info(String message, Object... arguments){
        log(message, Level.INFO, arguments);
    }
    public void info(String message, String stackTrace){
        log(message, Level.INFO, stackTrace);
    }
    public void info(String message, Throwable e){
        log(message, Level.INFO, e);
    }
    public void info(String message, Throwable e, Object ... arguments){
        log(message, Level.INFO, e, arguments);
    }
    public void debug(String message){
        log(message, Level.DEBUG);
    }
    public void debug(String message, Object... arguments){
        log(message, Level.DEBUG, arguments);
    }
    public void debug(String message, String stackTrace){
        log(message, Level.DEBUG, stackTrace);
    }
    public void debug(String message, Throwable e){
        log(message, Level.DEBUG, e);
    }
    public void debug(String message, Throwable e, Object ... arguments){
        log(message, Level.DEBUG, e, arguments);
    }
    public void trace(String message){
        log(message, Level.TRACE);
    }
    public void trace(String message, Object... arguments){
        log(message, Level.TRACE, arguments);
    }
    public void trace(String message, String stackTrace){
        log(message, Level.TRACE, stackTrace);
    }
    public void trace(String message, Throwable e){
        log(message, Level.TRACE, e);
    }
    public void trace(String message, Throwable e, Object ... arguments){
        log(message, Level.TRACE, e, arguments);
    }
}
