package com.veeva.vault.custom.app.model.xml;

public interface XMLElement {
    String getXPath();
    boolean isComment();

    boolean isStartElement();

    boolean isEndElement();

    XMLComment asComment();

    XMLStart asStartElement();

    XMLEnd asEndElement();

    String toString();
}
