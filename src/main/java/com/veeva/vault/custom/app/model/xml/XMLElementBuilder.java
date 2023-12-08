package com.veeva.vault.custom.app.model.xml;

import javax.xml.stream.XMLEventFactory;

public class XMLElementBuilder {
    private final XMLEventFactory eventFactory;

    public XMLElementBuilder(){
        this.eventFactory = XMLEventFactory.newInstance();
    }

    public XMLStart newStartElement(String startTagName) {
        return new XMLStart(startTagName);
    }

    public XMLEnd newEndElement(String endTagName) {
        return new XMLEnd(endTagName, null);
    }

    public XMLComment newComment(String commentText) {
        return new XMLComment(commentText);
    }
}
