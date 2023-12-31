package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.model.json.JsonProperty;
import com.veeva.vault.custom.app.model.json.JsonModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JsonClient {
    private ObjectMapper objectMapper;

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

    public <T extends JsonModel> T deserializeObject(String jsonString, Class<T> className) throws Exception{
        return objectMapper.readerFor(className).readValue(jsonString, className);
    }

    public <T extends JsonModel> String serializeObject(T model) throws Exception{
        return objectMapper.writeValueAsString(model);
    }

    public <T extends JsonModel> List<T> deserializeObjects(String jsonString, Class<T> className) throws Exception{
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, className);
        return objectMapper.readValue(jsonString, type);
    }

    public <T extends JsonModel> String serializeObjects(List<T> models) throws Exception{
        return objectMapper.writeValueAsString(models);
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
            return m instanceof AnnotatedField
                    && m.getAnnotation(JsonProperty.class) == null;
        }

    }

}
