package com.veeva.vault.custom.app.model.xml;

public interface XmlElement {
    String getXPath();
    boolean isComment();

    boolean isStartElement();

    boolean isEndElement();

    XmlComment asComment();

    XmlStart asStartElement();

    XmlEnd asEndElement();

    String toString();
}
