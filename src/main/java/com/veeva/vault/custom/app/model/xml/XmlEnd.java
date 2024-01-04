package com.veeva.vault.custom.app.model.xml;

/**
 * Represents an XML End Element
 */
public class XmlEnd implements XmlElement {
    private String xPath;
    private String name;
    private String characters;

    /**
     * Construct an XML End with the given name and characters
     * @param name
     * @param characters
     */
    public XmlEnd(String name, String characters) {
        this.characters = characters;
        this.name=name;
    }

    /**
     * @hidden
     * @param xPath
     * @param name
     * @param characters
     */
    public XmlEnd(String xPath, String name, String characters) {
        this.xPath = xPath;
        this.name=name;
        this.characters = characters;
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
     * Returns true if the XmlElement is an {@link XmlComment}, false otherwise
     * @return
     */
    @Override
    public boolean isComment() {
        return false;
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
        return true;
    }

    /**
     * Returns element as XmlComment if the XmlElement is a {@link XmlComment}
     * @return
     */
    @Override
    public XmlComment asComment() {
        return null;
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
        return this;
    }

    /**
     * Get the name of this element
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * Get the inner text of this element
     * @return
     */
    public String getCharacters() {
        return characters;
    }

    /**
     * Set the inner text of this element
     * @param characters
     */
    public void setCharacters(String characters){
        this.characters=characters;
    }
}
