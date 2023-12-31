package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.veeva.vault.custom.app.model.xml.*;
import com.veeva.vault.custom.app.model.xml.XmlReader;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class XmlClient {
    ObjectMapper objectMapper;

    /**
     * @hidden
     */
    public XmlClient(){
        SimpleModule module = new SimpleModule();
        module.addSerializer(XmlModel.class, new XmlSerializer());
        module.setSerializerModifier(new XmlSerializerModifier());
        objectMapper = new XmlMapper(new XmlFactory());
        objectMapper.setAnnotationIntrospector(new XmlAnnotationIntrospector());
        objectMapper.registerModule(module);
    }

    /**
     * Creates XmlReader instance from the designated File
     * @param file The input XML file to read
     * @return An XmlReader instance of the file
     * @throws Exception
     */
    public XmlReader readFile(com.veeva.vault.custom.app.model.files.File file) throws Exception{
        InputStream inputStream = new FileInputStream(file.getAbsolutePath());
        return new XmlReader(inputStream);
    }

    /**
     * Creates XmlReader instance from XML String
     * @param xmlString An XML structured string
     * @return An XmlReader instance of the string
     * @throws Exception
     */
    public XmlReader readString(String xmlString) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
        return new XmlReader(inputStream);
    }

    /**
     * Creates an XmlWriter instance from the designated file, and input reader
     * @param file The output XML file to write to
     * @param reader The XmlReader instance the file was read from
     * @return An XmlWriter instance of the file
     * @throws Exception
     */

    public XmlWriter openWriter(com.veeva.vault.custom.app.model.files.File file, XmlReader reader) throws Exception{
        return new XmlWriter(new FileOutputStream(file.getAbsolutePath()), reader);
    }

    /**
     * Creates an XmlWriter instance from the designated file, with the given encoding, version and DTD
     * @param file The output XML file to write to
     * @param encoding The encoding of the XML file, to be written in the <?xml> tag
     * @param version The version of the XML file, to be written in the <?xml> tag
     * @param dtd The DTD of the XML file, to be written in the <!DOCTYPE> tag
     * @return An XmlWriter instance of the file
     * @throws Exception
     */

    public XmlWriter openWriter(com.veeva.vault.custom.app.model.files.File file, String encoding, String version, String dtd) throws Exception{
        return new XmlWriter(new FileOutputStream(file.getAbsolutePath()), encoding, version, dtd);
    }

    public <T extends XmlModel> T deserializeObject(String xmlString, Class<T> className) throws Exception{
        return objectMapper.readerFor(className).readValue(xmlString, className);
    }

    public <T extends XmlModel> String serializeObject(T model) throws Exception{
        return objectMapper.writeValueAsString(model);
    }

    private static class XmlSerializer<T extends XmlModel> extends StdSerializer<T>{
        public XmlSerializer(){
            this(null);
        }

        protected XmlSerializer(Class<T> t) {
            super(t);
        }

        @Override
        public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            final ToXmlGenerator xmlGenerator = (ToXmlGenerator) jsonGenerator;
            Class modelClass = t.getClass();
            XmlModelInfo modelInfo = (XmlModelInfo) modelClass.getAnnotation(XmlModelInfo.class);
            if(modelInfo == null){
                throw new IOException("Unable to serialize class, annotation is missing or incorrect");
            }
            if(!modelInfo.key().isEmpty() && !modelInfo.key().isBlank()) {
                xmlGenerator.setNextName(new QName(modelInfo.key()));
            }
            xmlGenerator.writeStartObject();
            Arrays.stream(modelClass.getFields())
                    .filter(field -> field.getAnnotation(XmlAttribute.class)!=null)
                    .forEach(field -> {
                        field.setAccessible(true);
                        XmlAttribute property = field.getAnnotation(XmlAttribute.class);
                        try{
                            Object v = field.get(t);
                            xmlGenerator.setNextIsAttribute(true);
                            xmlGenerator.setNextName(new QName(property.key()));
                            xmlGenerator.writeStringField(property.key(), v.toString());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    });
            Arrays.stream(modelClass.getFields())
                    .filter(field -> field.getAnnotation(XmlProperty.class)!=null)
                    .filter(field -> field.getAnnotation(XmlProperty.class).order()!=-1)
                    .sorted(Comparator.comparingInt(field -> field.getAnnotation(XmlProperty.class).order()))
                    .forEach(field -> {
                        field.setAccessible(true);
                        XmlProperty property = field.getAnnotation(XmlProperty.class);
                        try {
                            Object v = field.get(t);
                            xmlGenerator.setNextIsAttribute(false);
                            xmlGenerator.setNextName(new QName(property.key()));
                            if(property.key().isEmpty() || property.key().isBlank()){
                                if(Arrays.stream(v.getClass().getInterfaces()).anyMatch(each -> each == XmlModel.class)){
                                    xmlGenerator.writeObjectField(property.key(), v);
                                }else {
                                    xmlGenerator.writeRaw(v.toString());
                                }
                            }else {
                                if (Arrays.stream(v.getClass().getInterfaces()).anyMatch(each -> each == XmlModel.class)) {
                                    xmlGenerator.writeObjectField(property.key(), v);
                                } else if (v.getClass().getSuperclass() == Number.class) {
                                    xmlGenerator.writeNumberField(property.key(), ((Number) v).byteValue());
                                } else if (v.getClass() == Boolean.class) {
                                    xmlGenerator.writeBooleanField(property.key(), (Boolean) v);
                                } else {
                                    xmlGenerator.writeStringField(property.key(), v.toString());
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    });
            Arrays.stream(modelClass.getFields())
                    .filter(field -> field.getAnnotation(XmlProperty.class)!=null)
                    .filter(field -> field.getAnnotation(XmlProperty.class).order()==-1)
                    .forEach(field -> {
                        field.setAccessible(true);
                        XmlProperty property = field.getAnnotation(XmlProperty.class);
                        xmlGenerator.setNextName(new QName(property.key()));
                        try {
                            Object v = field.get(t);
                            xmlGenerator.setNextIsAttribute(false);
                            xmlGenerator.setNextName(new QName(property.key()));
                            if(property.key().isEmpty() || property.key().isBlank()){
                                if(Arrays.stream(v.getClass().getInterfaces()).anyMatch(each -> each == XmlModel.class)){
                                    xmlGenerator.writeObjectField(property.key(), v);
                                }else {
                                    xmlGenerator.writeRaw(v.toString());
                                }
                            }else {
                                if (Arrays.stream(v.getClass().getInterfaces()).anyMatch(each -> each == XmlModel.class)) {
                                    xmlGenerator.writeObjectField(property.key(), v);
                                } else if (v.getClass().getSuperclass() == Number.class) {
                                    xmlGenerator.writeNumberField(property.key(), ((Number) v).byteValue());
                                } else if (v.getClass() == Boolean.class) {
                                    xmlGenerator.writeBooleanField(property.key(), (Boolean) v);
                                } else {
                                    xmlGenerator.writeStringField(property.key(), v.toString());
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    });
            xmlGenerator.writeEndObject();
        }
    }


    private static class XmlSerializerModifier<T extends XmlModel> extends BeanSerializerModifier {
        @Override
        public JsonSerializer<T> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer serializer) {
            return new XmlSerializer();
        }
    }

    private static class XmlAnnotationIntrospector extends JacksonXmlAnnotationIntrospector {
        @Override
        public Object findSerializer(Annotated a){
            if(Arrays.stream(a.getRawType().getInterfaces()).anyMatch(each -> each == XmlModel.class)) {
                return new XmlSerializer<>();
            }else{
                return null;
            }
        }
        @Override
        public PropertyName findNameForSerialization(Annotated a) {
            XmlProperty property = a.getAnnotation(XmlProperty.class);
            if (property == null) {
                return PropertyName.USE_DEFAULT;
            } else {
                return PropertyName.construct(property.key());
            }
        }

        @Override
        public JsonInclude.Value findPropertyInclusion(Annotated a){
            XmlProperty property = a.getAnnotation(XmlProperty.class);
            if (property != null && Arrays.stream(property.options()).anyMatch(xmlPropertyOption -> xmlPropertyOption == XmlPropertyOption.IGNORE_NULL)){
                return JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL);
            }else {
                return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
            }
        }

        @Override
        public String[] findSerializationPropertyOrder(AnnotatedClass ac){
            List<String> fields = new ArrayList<String>();
            StreamSupport.stream(ac.fields().spliterator(), false)
                    .filter(field -> field.getAnnotation(XmlProperty.class)!=null)
                    .filter(field -> field.getAnnotation(XmlProperty.class).order()!=-1)
                    .sorted(Comparator.comparingInt(field -> field.getAnnotation(XmlProperty.class).order()))
                    .forEach(field -> {
                        fields.add(field.getName());
                    });
            StreamSupport.stream(ac.fields().spliterator(), false)
                    .filter(field -> field.getAnnotation(XmlProperty.class)!=null)
                    .filter(field -> field.getAnnotation(XmlProperty.class).order()==-1)
                    .forEach(field -> {
                        fields.add(field.getName());
                    });
            return fields.toArray(new String[fields.size()]);
        }

        @Override
        public PropertyName findRootName(AnnotatedClass ac){
            XmlModelInfo modelInfo = ac.getAnnotations().get(XmlModelInfo.class);
            if (modelInfo == null) {
                return PropertyName.USE_DEFAULT;
            }else{
                return PropertyName.construct(modelInfo.key());
            }
        }

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            XmlProperty property = a.getAnnotation(XmlProperty.class);
            XmlAttribute attribute = a.getAnnotation(XmlAttribute.class);
            if (property != null) {
                if (Arrays.stream(a.getRawType().getInterfaces()).anyMatch(each -> each == XmlModel.class)) {
                    XmlModelInfo modelInfo = a.getRawType().getAnnotation(XmlModelInfo.class);
                    if(!modelInfo.key().isBlank() && !modelInfo.key().isEmpty()) {
                        return PropertyName.construct(modelInfo.key());
                    }
                }
                if(!property.key().isBlank() && !property.key().isEmpty()) {
                    return PropertyName.construct(property.key());
                }else{
                    Class parentClass = ((AnnotatedMember)a).getDeclaringClass();
                    if(parentClass!=null){
                        XmlModelInfo modelInfo = (XmlModelInfo) parentClass.getAnnotation(XmlModelInfo.class);
                        if(modelInfo!=null && !modelInfo.key().isBlank() && !modelInfo.key().isEmpty()) {
                            return PropertyName.construct(modelInfo.key());
                        }else{
                            return PropertyName.NO_NAME;
                        }
                    }
                }
            } else if (attribute != null) {
                if(!attribute.key().isBlank() && !attribute.key().isEmpty()) {
                    return PropertyName.construct(attribute.key());
                }else{
                    return PropertyName.NO_NAME;
                }
            }
            return PropertyName.USE_DEFAULT;
        }

        @Override
        public Boolean isOutputAsText(MapperConfig<?> config, Annotated a){
            XmlProperty property = a.getAnnotation(XmlProperty.class);
            if(property != null && (property.key().isBlank() || property.key().isEmpty())) {
                Class parentClass = ((AnnotatedMember)a).getDeclaringClass();
                if(parentClass!=null){
                    XmlModelInfo modelInfo = (XmlModelInfo) parentClass.getAnnotation(XmlModelInfo.class);
                    if(modelInfo!=null && !modelInfo.key().isBlank() && !modelInfo.key().isEmpty()) {
                        return false;
                    }else{
                        return true;
                    }
                }else{
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            if(m instanceof AnnotatedField){
                XmlProperty property = m.getAnnotation(XmlProperty.class);
                XmlAttribute attribute = m.getAnnotation(XmlAttribute.class);
                if(property!=null && Arrays.stream(property.options()).anyMatch(xmlPropertyOption -> xmlPropertyOption == XmlPropertyOption.IGNORE_ALL)){
                    return true;
                }else if(property==null && attribute==null){
                    return true;
                }
            }
            return false;
        }
    }
}

