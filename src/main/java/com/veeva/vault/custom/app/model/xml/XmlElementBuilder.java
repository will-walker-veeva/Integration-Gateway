package com.veeva.vault.custom.app.model.xml;

import javax.xml.stream.XMLEventFactory;

/**
 * Element Builder helper class
 */
public class XmlElementBuilder {
    private final XMLEventFactory eventFactory;

    public XmlElementBuilder(){
        this.eventFactory = XMLEventFactory.newInstance();
    }

    /**
     * Returns a new Start Element for the given tag name
     * @param startTagName
     * @return
     */
    public XmlStart newStartElement(String startTagName) {
        return new XmlStart(startTagName);
    }

    /**
     * Returns a new End Element for the given tag name
     * @param endTagName
     * @return
     */
    public XmlEnd newEndElement(String endTagName) {
        return new XmlEnd(endTagName, null);
    }

    /**
     * Returns a new Comment Element with the given text
     * @param commentText
     * @return
     */
    public XmlComment newComment(String commentText) {
        return new XmlComment(commentText);
    }
}
