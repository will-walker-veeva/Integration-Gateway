package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.admin.templateprocessor.TemplateProcessorDialect;
import com.veeva.vault.custom.app.model.json.JsonObject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TemplateProcessorClient {
    private TemplateProcessorClient(){

    }

    /**
     * @hidden
     * @return
     */
    public static TemplateProcessorClient newInstance(){
        return new TemplateProcessorClient();
    }

    /**
     * @hidden
     */
    private static String processTemplate(String template, JsonObject properties, TemplateMode templateMode) throws ProcessException {
        Context context = null;
        try{
            context = getContext(properties.toString());
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
        TemplateEngine templateEngine = getTemplateEngine(templateMode);
        StringWriter stringWriter = new StringWriter();
        try{
            templateEngine.process(template, context, stringWriter);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
        return stringWriter.toString();
    }

    /**
     *
     * @param template
     * @param properties
     * @return
     * @throws ProcessException
     */

    public static String processXmlTemplate(String template, JsonObject properties) throws ProcessException {
        return processTemplate(template, properties, TemplateMode.XML);
    }

    /**
     *
     * @param template
     * @param properties
     * @return
     * @throws ProcessException
     */
    public static String processHtmlTemplate(String template, JsonObject properties) throws ProcessException {
        return processTemplate(template, properties, TemplateMode.HTML);
    }

    /**
     *
     * @param template
     * @param properties
     * @return
     * @throws ProcessException
     */
    public static String processTextTemplate(String template, JsonObject properties) throws ProcessException {
        return processTemplate(template, properties, TemplateMode.TEXT);
    }

    

    private static Context getContext(String dataString) throws Exception {
        Map<String, Object> modelMap = new ObjectMapper().readValue(dataString, HashMap.class);
        Context context = new Context(Locale.ENGLISH, modelMap);
        return context;
    }

    private static TemplateEngine getTemplateEngine(TemplateMode templateMode){
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(templateMode);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addDialect(new Java8TimeDialect());
        templateEngine.addDialect(new TemplateProcessorDialect());
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
