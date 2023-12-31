package com.veeva.vault.custom.app.model.xml;


import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.OutputStream;
import java.util.*;

/**
 * The XmlWriter class specifies how to write XML.
 */
public class XmlWriter implements AutoCloseable{
    private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    private XMLEventWriter xmlEventWriter = null;
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private Map<String,String> qNameToprefixMap;
    private List<Namespace> namespacesList = new ArrayList<Namespace>();

    /**
     * @hidden
     * @param outputStream
     * @param reader
     * @throws Exception
     */
    public XmlWriter(OutputStream outputStream, XmlReader reader) throws Exception{
        xmlEventWriter = xmlOutputFactory.createXMLEventWriter(outputStream);
        reader.initiate();
        if(reader.getEncoding()!=null) xmlEventWriter.add(eventFactory.createStartDocument(reader.getEncoding(), reader.getVersion()));
        if(reader.getDTD()!=null) xmlEventWriter.add(eventFactory.createDTD(reader.getDTD()));
    }

    /**
     * @hidden
     * @param outputStream
     * @param encoding
     * @param version
     * @param dtd
     * @throws Exception
     */
    public XmlWriter(OutputStream outputStream, String encoding, String version, String dtd) throws Exception{
        xmlEventWriter = xmlOutputFactory.createXMLEventWriter(outputStream);
        if(encoding!=null) xmlEventWriter.add(eventFactory.createStartDocument(encoding, version));
        if(dtd!=null) xmlEventWriter.add(eventFactory.createDTD(dtd));
    }

    /**
     * Add an element to the output stream
     * @param xmlElement
     * @throws Exception
     */
    public void add(XmlElement xmlElement) throws Exception {
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

    /**
     * Adds an entire stream to an output stream. This should be treated as a convenience method that will perform the following loop over all the elements in a Xml Reader and call add on each element.
     * @param xmlReader
     * @throws Exception
     */
    public void add(XmlReader xmlReader) throws Exception {
        while (xmlReader.hasNext()) {
            XmlElement next = xmlReader.getNext();
            add(next);
        }
    }

    /**
     * Frees any resources associated with this stream
     * @throws Exception
     */
    public void close() throws Exception{
        this.xmlEventWriter.flush();
        this.xmlEventWriter.close();
    }
}