package com.veeva.vault.custom.app.model.xml;

public class XMLComment implements XMLElement {
    private String xPath;
    private String text;

    public XMLComment(String text){
        this.text = text;
    }

    public XMLComment(String xPath, String text){
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
    public XMLComment asComment() {
        return this;
    }

    @Override
    public XMLStart asStartElement() {
        return null;
    }

    @Override
    public XMLEnd asEndElement() {
        return null;
    }
}
