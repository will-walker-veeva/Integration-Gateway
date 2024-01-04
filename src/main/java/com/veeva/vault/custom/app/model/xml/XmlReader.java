package com.veeva.vault.custom.app.model.xml;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.InputStream;
import java.util.*;

/**
 * The XmlReader interface allows forward, read-only access to XML. It is designed to be the lowest level and most efficient way to read XML data.
 */
public class XmlReader {
    private XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private String dtd = null;
    private String encoding = null;
    private String version = null;
    private XMLEventReader reader = null;
    private LinkedList<String> startingXPathList = new LinkedList<>();
    private XmlElement previousEvent;
    private String characters;
    public XmlReader(InputStream inputStream) throws Exception{
        this.reader = xmlInputFactory.createXMLEventReader(inputStream);
    }

    /**
     * Returns true if there are more parsing elements and false if there are no more elements.
     * @return
     * @throws Exception
     */
    public boolean hasNext() throws Exception{
        XMLEvent peek = reader.peek();
        if (peek.isEndDocument()){
            return false;
        }
        return this.reader.hasNext();
    }

    /**
     * Returns the next XmlElement
     * @return
     * @throws Exception
     */

    public XmlElement getNext() throws Exception{
        XMLEvent event = this.reader.nextEvent();
        XmlElement response = null;
        String xPATH = null;
        if(event.isStartElement()){
            StartElement startElement = event.asStartElement();
            Map<String, String> attributeMap = new HashMap<>();
            Iterator<Attribute> attributes = startElement.getAttributes();
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                attributeMap.put(attribute.getName().toString(), attribute.getValue());
            }
            startingXPathList.addLast(startElement.getName().getLocalPart());
            xPATH = "/" + String.join("/", startingXPathList);
            XmlStart startResponse = new XmlStart(xPATH, startElement.getName().getLocalPart(), attributeMap);
            startResponse.setQName(startElement.getName());
            startResponse.setNamespaces(startResponse.getNamespaces());
            response = startResponse;
        }else if(event.isCharacters()){
            XMLEvent peek = reader.peek();
            Characters charactersObj = event.asCharacters();
            if (charactersObj.getData().equals("\n") && (previousEvent.isStartElement() || previousEvent.isEndElement() || previousEvent.isComment())) {
                return getNext(); // move to next
            }
            characters = characters != null ? characters + charactersObj.getData() : charactersObj.getData();
            if (peek.isEndElement() || peek.isCharacters()) {
                return getNext();
            }
            xPATH = "/" + String.join("/", startingXPathList);
            response = new XmlComment(xPATH, characters);
        }else if(event.isEndElement()){
            xPATH = "/" + String.join("/", startingXPathList);
            startingXPathList.removeLast();
            response = new XmlEnd(xPATH, event.asEndElement().getName().getLocalPart(), characters);
        }else if (event.getEventType() == XMLEvent.COMMENT &&  ((javax.xml.stream.events.Comment) event).getText()!=null){
            xPATH = "/" + String.join("/", startingXPathList);
            response = new XmlComment(xPATH, ((javax.xml.stream.events.Comment) event).getText());
        }
        if(response==null){
            return getNext();
        }
        characters = null;
        previousEvent = response;
        return response;
    }

    /**
     * @hidden
     * @return
     */
    protected XMLEventReader getReader(){
        return this.reader;
    }

    /**
     * @hidden
     * @return
     */
    protected String getDTD(){
        return this.dtd;
    }

    /**
     * @hidden
     * @throws Exception
     */
    protected void initiate() throws Exception{
        if(reader.hasNext() && reader.peek().getEventType() == XMLStreamConstants.START_DOCUMENT){
            XMLEvent event = this.reader.nextEvent();
            this.encoding = ((javax.xml.stream.events.StartDocument) event).getCharacterEncodingScheme();
            this.version = ((javax.xml.stream.events.StartDocument) event).getVersion();
            if(reader.hasNext() && reader.peek().getEventType() == XMLStreamConstants.DTD){
                event = this.reader.nextEvent();
                this.dtd =  event.toString();
            }
        }
    }

    /**
     * @hidden
     * @return
     */
    protected String getEncoding() {
        return encoding;
    }

    /**
     * @hidden
     * @return
     */
    protected String getVersion() {
        return version;
    }
}
