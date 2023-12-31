package com.veeva.vault.custom.app.model.xml;

public class XmlEnd implements XmlElement {
    private String xPath;
    private String name;
    private String characters;

    public XmlEnd(String name, String characters) {
        this.characters = characters;
        this.name=name;
    }

    public XmlEnd(String xPath, String name, String characters) {
        this.xPath = xPath;
        this.name=name;
        this.characters = characters;
    }

    @Override
    public String getXPath() {
        return xPath;
    }

    @Override
    public boolean isComment() {
        return false;
    }

    @Override
    public boolean isStartElement() {
        return false;
    }

    @Override
    public boolean isEndElement() {
        return true;
    }

    @Override
    public XmlComment asComment() {
        return null;
    }

    @Override
    public XmlStart asStartElement() {
        return null;
    }

    @Override
    public XmlEnd asEndElement() {
        return this;
    }

    public String getName(){
        return name;
    }

    public String getCharacters() {
        return characters;
    }

    public void setCharacters(String characters){
        this.characters=characters;
    }
}
