package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.model.json.JsonElement;
import com.veeva.vault.custom.app.model.json.JsonModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Service
public class JsonClient {
    private ObjectMapper objectMapper;

    /**
     * @hidden
     */
    protected JsonClient(){
        objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonModel.class, new JsonSerializer());
        objectMapper.registerModule(module);
    }

    public <T extends JsonModel> T serializeObject(String jsonString, Class<T> className) throws Exception{
        return objectMapper.readerFor(className).readValue(jsonString, className);
    }

    public <T extends JsonModel> String deserializeObject(T model) throws Exception{
        return objectMapper.writeValueAsString(model);
    }

    public <T extends JsonModel> List<T> serializeObjects(String jsonString, Class<T> className) throws Exception{
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, className);
        return objectMapper.readValue(jsonString, type);
    }

    public <T extends JsonModel> String deserializeObjects(List<T> models) throws Exception{
        return objectMapper.writeValueAsString(models);
    }

    private static class CustomAnnotationIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            JsonElement property = a.getAnnotation(JsonElement.class);
            if (property == null) {
                return PropertyName.USE_DEFAULT;
            } else {
                return PropertyName.construct(property.key());
            }
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            return m instanceof AnnotatedField
                    && m.getAnnotation(JsonElement.class) == null;
        }
    }

    private class JsonSerializer<T extends JsonModel> extends StdSerializer<T> {
        public JsonSerializer() {
            this(null);
        }

        public JsonSerializer(Class<T> t) {
            super(t);
        }

        @Override
        public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            Field[] fields = t.getClass().getDeclaredFields();
            for (Field f : fields) {
                JsonElement ann = f.getAnnotation(JsonElement.class);
                if(ann!=null) {
                    try {
                        Class thisClass = f.getType();
                        f.setAccessible(true);
                        Object v = f.get(t);
                        switch(thisClass.getSimpleName()){
                            case "String":
                                jsonGenerator.writeStringField(ann.key(), (String) v);
                                break;
                            case "Integer":
                                jsonGenerator.writeNumberField(ann.key(), (Integer) v);
                                break;
                            case "Boolean":
                                jsonGenerator.writeBooleanField(ann.key(), (Boolean) v);
                                break;
                            default:
                                jsonGenerator.writeObjectField(ann.key(), v.toString());
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            jsonGenerator.writeEndObject();
        }

    }

}
