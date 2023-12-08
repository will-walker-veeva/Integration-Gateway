package com.veeva.vault.custom.app.model.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class XMLReader {
    private XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private XMLEventReader reader = null;
    private LinkedList<String> startingXPathList = new LinkedList<>();
    private XMLElement previousEvent;
    private String characters;
    public XMLReader(InputStream inputStream) throws Exception{
        this.reader = xmlInputFactory.createXMLEventReader(inputStream);
    }

    public boolean hasNext(){
        return this.reader.hasNext();
    }

    public XMLElement getNext() throws Exception{
        XMLEvent event = this.reader.nextEvent();
        XMLElement response = null;
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
            XMLStart startResponse = new XMLStart(xPATH, startElement.getName().getLocalPart(), attributeMap);
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
            response = new XMLComment(xPATH, characters);
        }else if(event.isEndElement()){
            xPATH = "/" + String.join("/", startingXPathList);
            startingXPathList.removeLast();
            response = new XMLEnd(xPATH, event.asEndElement().getName().getLocalPart(), characters);
        }
        characters = null;
        previousEvent = response;
        return response;
    }

    protected XMLEventReader getReader(){
        return this.reader;
    }

}
