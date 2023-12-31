package com.veeva.vault.custom.app.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.xml.*;
import com.veeva.vault.custom.app.model.xml.XmlReader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class XmlClient {
    private ObjectMapper objectMapper;

    /**
     * @hidden
     */
    public XmlClient(){
        SimpleModule module = new SimpleModule();
        objectMapper = new XmlMapper(new XmlFactory());
        objectMapper.setAnnotationIntrospector(new XmlAnnotationIntrospector());
        objectMapper.registerModule(module);
    }

    /**
     * Creates XmlReader instance from the designated File
     * @param file The input XML file to read
     * @return An XmlReader instance of the file
     * @throws ProcessException
     */
    public XmlReader readFile(com.veeva.vault.custom.app.model.files.File file) throws ProcessException {
        try(InputStream inputStream = new FileInputStream(file.getAbsolutePath())){
            return new XmlReader(inputStream);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     * Creates XmlReader instance from XML String
     * @param xmlString An XML structured string
     * @return An XmlReader instance of the string
     * @throws ProcessException
     */
    public XmlReader readString(String xmlString) throws ProcessException {
        try(InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes())){
            return new XmlReader(inputStream);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }

    }

    /**
     * Creates an XmlWriter instance from the designated file, and input reader
     * @param file The output XML file to write to
     * @param reader The XmlReader instance the file was read from
     * @return An XmlWriter instance of the file
     * @throws ProcessException
     */

    public XmlWriter openWriter(com.veeva.vault.custom.app.model.files.File file, XmlReader reader) throws ProcessException {
        try(OutputStream outputStream = new FileOutputStream(file.getAbsolutePath())){
            return new XmlWriter(outputStream, reader);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    /**
     * Creates an XmlWriter instance from the designated file, with the given encoding, version and DTD
     * @param file The output XML file to write to
     * @param encoding The encoding of the XML file, to be written in the <?xml> tag
     * @param version The version of the XML file, to be written in the <?xml> tag
     * @param dtd The DTD of the XML file, to be written in the <!DOCTYPE> tag
     * @return An XmlWriter instance of the file
     * @throws ProcessException
     */

    public XmlWriter openWriter(com.veeva.vault.custom.app.model.files.File file, String encoding, String version, String dtd) throws ProcessException {
        try(OutputStream outputStream = new FileOutputStream(file.getAbsolutePath())) {
            return new XmlWriter(outputStream, encoding, version, dtd);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends XmlModel> T deserializeObject(String xmlString, Class<T> className) throws ProcessException {
        try{
            return objectMapper.readerFor(className).readValue(xmlString, className);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    public <T extends XmlModel> String serializeObject(T model) throws ProcessException {
        try{
            return objectMapper.writeValueAsString(model);
        }catch(Exception e){
            throw new ProcessException(e.getMessage());
        }
    }

    private static class XmlAnnotationIntrospector extends JacksonXmlAnnotationIntrospector {
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
        public Boolean isOutputAsAttribute(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, com.fasterxml.jackson.databind.introspect.Annotated a){
            XmlAttribute attribute = a.getAnnotation(XmlAttribute.class);
            if (attribute != null) {
                return true;
            }
            return false;
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

