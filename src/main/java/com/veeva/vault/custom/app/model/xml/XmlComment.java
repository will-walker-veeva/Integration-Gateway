package com.veeva.vault.custom.app.model.xml;

public class XmlComment implements XmlElement {
    private String xPath;
    private String text;

    public XmlComment(String text){
        this.text = text;
    }

    public XmlComment(String xPath, String text){
        this.xPath = xPath;
        this.text = text;
    }

    @Override
    public String getXPath() {
        return xPath;
    }

    public String getText(){
        return this.text;
    }

    @Override
    public boolean isComment() {
        return true;
    }

    @Override
    public boolean isStartElement() {
        return false;
    }

    @Override
    public boolean isEndElement() {
        return false;
    }

    @Override
    public XmlComment asComment() {
        return this;
    }

    @Override
    public XmlStart asStartElement() {
        return null;
    }

    @Override
    public XmlEnd asEndElement() {
        return null;
    }
}
