package com.veeva.vault.custom.app.model.xml;

/**
 * This is the base element interface for handling Xml Elements
 */
public interface XmlElement {
    /**
     * Get the XPath for this element
     * @return
     */
    String getXPath();

    /**
     * Returns true if the XmlElement is an {@link XmlComment}, false otherwise
     * @return
     */
    boolean isComment();

    /**
     * Returns true if the XmlElement is an {@link XmlStart}, false otherwise
     * @return
     */
    boolean isStartElement();

    /**
     * Returns true if the XmlElement is an {@link XmlEnd}, false otherwise
     * @return
     */
    boolean isEndElement();

    /**
     * Returns element as XmlComment if the XmlElement is a {@link XmlComment}
     * @return
     */
    XmlComment asComment();

    /**
     * Returns element as XmlStart if the XmlElement is a {@link XmlStart}
     * @return
     */
    XmlStart asStartElement();

    /**
     * Returns element as XmlEnd if the XmlElement is a {@link XmlEnd}
     * @return
     */
    XmlEnd asEndElement();

    String toString();
}