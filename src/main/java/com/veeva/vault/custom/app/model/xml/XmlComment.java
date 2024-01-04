package com.veeva.vault.custom.app.model.xml;

/**
 * Class representing an XmlComment
 */
public class XmlComment implements XmlElement {
    private String xPath;
    private String text;

    /**
     * Construct an XML Comment with the given text
     * @param text
     */
    public XmlComment(String text){
        this.text = text;
    }

    /**
     * @hidden
     * @param text
     */
    public XmlComment(String xPath, String text){
        this.xPath = xPath;
        this.text = text;
    }
    /**
     * Get the XPath for this element
     * @return
     */
    @Override
    public String getXPath() {
        return xPath;
    }

    /**
     * Get the Comment Text
     * @return
     */
    public String getText(){
        return this.text;
    }
    /**
     * Returns true if the XmlElement is an {@link XmlComment}, false otherwise
     * @return
     */
    @Override
    public boolean isComment() {
        return true;
    }
    /**
     * Returns true if the XmlElement is an {@link XmlStart}, false otherwise
     * @return
     */
    @Override
    public boolean isStartElement() {
        return false;
    }
    /**
     * Returns true if the XmlElement is an {@link XmlEnd}, false otherwise
     * @return
     */
    @Override
    public boolean isEndElement() {
        return false;
    }

    /**
     * Returns element as XmlComment if the XmlElement is a {@link XmlComment}
     * @return
     */
    @Override
    public XmlComment asComment() {
        return this;
    }

    /**
     * Returns element as XmlStart if the XmlElement is a {@link XmlStart}
     * @return
     */
    @Override
    public XmlStart asStartElement() {
        return null;
    }

    /**
     * Returns element as XmlEnd if the XmlElement is a {@link XmlEnd}
     * @return
     */
    @Override
    public XmlEnd asEndElement() {
        return null;
    }
}
