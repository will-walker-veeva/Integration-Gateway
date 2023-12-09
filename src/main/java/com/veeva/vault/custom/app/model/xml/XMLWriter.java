package com.veeva.vault.custom.app.model.xml;


import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.OutputStream;
import java.util.*;

public class XMLWriter implements AutoCloseable{
    private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    private XMLEventWriter xmlEventWriter = null;
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private Map<String,String> qNameToprefixMap;
    private List<Namespace> namespacesList = new ArrayList<Namespace>();

    public XMLWriter(OutputStream outputStream, XMLReader reader) throws Exception{
        xmlEventWriter = xmlOutputFactory.createXMLEventWriter(outputStream);
        reader.initiate();
        if(reader.getEncoding()!=null) xmlEventWriter.add(eventFactory.createStartDocument(reader.getEncoding(), reader.getVersion()));
        if(reader.getDTD()!=null) xmlEventWriter.add(eventFactory.createDTD(reader.getDTD()));
    }

    public XMLWriter(OutputStream outputStream, String encoding, String version, String dtd) throws Exception{
        xmlEventWriter = xmlOutputFactory.createXMLEventWriter(outputStream);
        if(encoding!=null) xmlEventWriter.add(eventFactory.createStartDocument(encoding, version));
        if(dtd!=null) xmlEventWriter.add(eventFactory.createDTD(dtd));
    }

    public void add(XMLElement xmlElement) throws Exception {
        try {
            if (xmlElement.isStartElement()) {
                StartElement startElement;
                if(xmlElement.asStartElement().getName().equals("MCCI_IN200100UV01")){
                    Iterator<Namespace> namespaces = xmlElement.asStartElement().getNamespaces();
                    for (Iterator<Namespace> it = namespaces; it.hasNext(); ) {
                        Namespace namespace = it.next();
                        namespacesList.add(namespace);
                        qNameToprefixMap = new HashMap<>();
                        qNameToprefixMap.put("{"+namespace.getValue()+"}",namespace.getPrefix());
                    }
                    startElement = eventFactory.createStartElement( xmlElement.asStartElement().getQName(), null, namespacesList.iterator());
                }else{
                    startElement = eventFactory.createStartElement("","",xmlElement.asStartElement().getName());
                }

                xmlEventWriter.add(startElement);
                Map<String, String> attributes = xmlElement.asStartElement().getAttributes();
                for (String attributeName : attributes.keySet()) {
                    String attributeValue = attributes.get(attributeName);
                    if(qNameToprefixMap!=null){
                        for(String qName : qNameToprefixMap.keySet()){
                            if(attributeName.contains(qName)){
                                String preFix = qNameToprefixMap.get(qName);
                                attributeName = attributeName.replace(qName,preFix+":");
                                break;
                            }

                        }
                    }
                    Attribute attribute = eventFactory.createAttribute(attributeName, attributeValue);
                    xmlEventWriter.add(attribute);
                }
            } else if (xmlElement.isEndElement()) {
                String characters = xmlElement.asEndElement().getCharacters();
                if (characters != null) {
                    Characters charactersObj = eventFactory.createCharacters(characters);
                    xmlEventWriter.add(charactersObj);
                }
                xmlEventWriter.add(eventFactory.createEndElement("", "", xmlElement.asEndElement().getName()));
            }else if (xmlElement.isComment()){
                xmlEventWriter.add(eventFactory.createComment(xmlElement.asComment().getText()));
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

    }

    public void add(XMLReader xmlReader) throws Exception {
        while (xmlReader.hasNext()) {
            XMLElement next = xmlReader.getNext();
            add(next);
        }
    }

    public void close() throws Exception{
        this.xmlEventWriter.flush();
        this.xmlEventWriter.close();
    }
}