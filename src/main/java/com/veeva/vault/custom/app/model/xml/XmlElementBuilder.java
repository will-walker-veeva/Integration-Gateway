package com.veeva.vault.custom.app.model.xml;

import javax.xml.stream.XMLEventFactory;

public class XmlElementBuilder {
    private final XMLEventFactory eventFactory;

    public XmlElementBuilder(){
        this.eventFactory = XMLEventFactory.newInstance();
    }

    public XmlStart newStartElement(String startTagName) {
        return new XmlStart(startTagName);
    }

    public XmlEnd newEndElement(String endTagName) {
        return new XmlEnd(endTagName, null);
    }

    public XmlComment newComment(String commentText) {
        return new XmlComment(commentText);
    }
}
