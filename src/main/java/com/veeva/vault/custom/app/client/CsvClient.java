package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.core.AnyGetter;
import com.veeva.vault.custom.app.model.core.AnySetter;
import com.veeva.vault.custom.app.model.csv.CsvModel;
import com.veeva.vault.custom.app.model.csv.CsvProperty;
import com.veeva.vault.custom.app.model.csv.CsvPropertyOption;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.custom.app.model.json.JsonArray;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client for CSV operations, reading CSV files and mapping to/from CSV models
 *  See also {@link com.veeva.vault.custom.app.model.csv}<br>
 */
@Service
public class CsvClient {
    private ObjectMapper objectMapper;
    private FilesClient filesClient = new FilesClient();

    /**
     * @hidden
     */
    protected CsvClient(){
        objectMapper = new CsvMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);
    }

    public <T extends CsvModel> List<T> deserializeObjects(String csvString, Class<T> className) throws ProcessException {
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, className);
        try {
            return objectMapper.readValue(csvString, type);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends CsvModel> String serializeObjects(Collection<T> models) throws ProcessException {
        try{
            CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
            Class<T> classModel = (Class<T>) models.stream().findFirst().get().getClass();
            Arrays.stream(classModel.getFields())
                    .filter(field -> field.getAnnotation(CsvProperty.class)!=null)
                    .map(field -> field.getAnnotation(CsvProperty.class))
                    .map(field -> field.key())
                    .sorted(Comparator.comparing(el -> el))
                    .forEach(field -> csvSchemaBuilder.addColumn(field));
            CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(models.getClass(), classModel);
            return objectMapper.writerFor(type).with(csvSchema).writeValueAsString(models);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public JsonArray readFile(File file, Charset charset) throws ProcessException {
        String csvString = filesClient.readFileToString(file, charset);
        return JsonArray.ofCsvString(csvString);
    }

    private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public PropertyName findNameForSerialization(Annotated a) {
            CsvProperty property = a.getAnnotation(CsvProperty.class);
            if (property == null) {
                return PropertyName.USE_DEFAULT;
            } else {
                return PropertyName.construct(property.key());
            }
        }

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            CsvProperty property = a.getAnnotation(CsvProperty.class);
            if (property == null) {
                return PropertyName.USE_DEFAULT;
            } else {
                return PropertyName.construct(property.key());
            }
        }


        @Override
        public List<PropertyName> findPropertyAliases(Annotated m){
            CsvProperty property = m.getAnnotation(CsvProperty.class);
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
        public Boolean findSerializationSortAlphabetically(Annotated m){
            return true;
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            if(m instanceof AnnotatedField){
                CsvProperty property = m.getAnnotation(CsvProperty.class);
                if(property!=null && Arrays.stream(property.options()).anyMatch(csvPropertyOption -> csvPropertyOption == CsvPropertyOption.IGNORE_ALL)){
                    return true;
                }else if(property==null){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Boolean hasAnyGetter(Annotated a){
            AnyGetter property = a.getAnnotation(AnyGetter.class);
            if(property!=null){
                return true;
            }
            return false;
        }

        @Override
        public Boolean hasAnySetter(Annotated a){
            AnySetter property = a.getAnnotation(AnySetter.class);
            if(property!=null){
                return true;
            }
            return false;
        }

    }

}
