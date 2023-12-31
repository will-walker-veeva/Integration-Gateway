package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.json.JsonProperty;
import com.veeva.vault.custom.app.model.json.JsonModel;
import com.veeva.vault.custom.app.model.json.JsonPropertyOption;
import org.springframework.stereotype.Service;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JsonClient {
    private ObjectMapper objectMapper;
    private FilesClient filesClient = new FilesClient();

    /**
     * @hidden
     */
    protected JsonClient(){
        objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setAnnotationIntrospector(new JsonAnnotationIntrospector());
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);
    }

    public <T extends JsonModel> T deserializeObject(String jsonString, Class<T> className) throws ProcessException {
        try{
            return objectMapper.readerFor(className).readValue(jsonString, className);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends JsonModel> String serializeObject(T model) throws ProcessException {
        try{
            return objectMapper.writeValueAsString(model);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends JsonModel> List<T> deserializeObjects(String jsonString, Class<T> className) throws ProcessException {
        try{
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, className);
            return objectMapper.readValue(jsonString, type);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }

    }

    public <T extends JsonModel> String serializeObjects(List<T> models) throws ProcessException {
        try{
            return objectMapper.writeValueAsString(models);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws ProcessException
     */
    public JsonObject readFile(File file) throws ProcessException {
        return new JsonObject(filesClient.readFileToString(file, StandardCharsets.UTF_8));
    }

    private static class JsonAnnotationIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public PropertyName findNameForSerialization(Annotated a) {
            JsonProperty property = a.getAnnotation(JsonProperty.class);
            if (property == null) {
                return PropertyName.USE_DEFAULT;
            } else {
                return PropertyName.construct(property.key());
            }
        }

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            JsonProperty property = a.getAnnotation(JsonProperty.class);
            if (property == null) {
                return PropertyName.USE_DEFAULT;
            } else {
                return PropertyName.construct(property.key());
            }
        }

        @Override
        public JsonInclude.Value findPropertyInclusion(Annotated a){
            JsonProperty property = a.getAnnotation(JsonProperty.class);
            if (property != null && Arrays.stream(property.options()).anyMatch(jsonPropertyOption -> jsonPropertyOption == JsonPropertyOption.IGNORE_NULL)){
                return JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL);
            }else {
                return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
            }
        }

        @Override
        public List<PropertyName> findPropertyAliases(Annotated m){
            JsonProperty property = m.getAnnotation(JsonProperty.class);
            if (property == null) {
                return null;
            } else {
                List<PropertyName> propertyNames = Arrays.stream(property.aliases())
                        .filter(each -> !each.isEmpty() && !each.isBlank())
                        .map(each -> PropertyName.construct(each))
                        .collect(Collectors.toList());
                return propertyNames;
            }
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            if(m instanceof AnnotatedField){
                JsonProperty property = m.getAnnotation(JsonProperty.class);
                if(property!=null && Arrays.stream(property.options()).anyMatch(jsonPropertyOption -> jsonPropertyOption == JsonPropertyOption.IGNORE_ALL)){
                    return true;
                }else if(property==null){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Boolean hasAnyGetter(Annotated a){
            JsonProperty property = a.getAnnotation(JsonProperty.class);
            JsonAnyGetter anyGetter = a.getAnnotation(JsonAnyGetter.class);
            if(property!=null && Arrays.stream(property.options()).anyMatch(jsonPropertyOption -> jsonPropertyOption == JsonPropertyOption.ANY_GETTER)){
                return true;
            }else if(anyGetter!=null){
                return true;
            }
            return false;
        }

        @Override
        public Boolean hasAnySetter(Annotated a){
            JsonProperty property = a.getAnnotation(JsonProperty.class);
            JsonAnySetter anySetter = a.getAnnotation(JsonAnySetter.class);
            if(property!=null && Arrays.stream(property.options()).anyMatch(jsonPropertyOption -> jsonPropertyOption == JsonPropertyOption.ANY_SETTER)){
                return true;
            }else if(anySetter!=null){
                return true;
            }
            return false;
        }

    }

}
