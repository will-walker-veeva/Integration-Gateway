package com.veeva.vault.custom.app.model.xml;

public class XMLEnd implements XMLElement {
    private String xPath;
    private String name;
    private String characters;

    public XMLEnd(String name, String characters) {
        this.characters = characters;
        this.name=name;
    }

    public XMLEnd(String xPath, String name, String characters) {
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
    public XMLComment asComment() {
        return null;
    }

    @Override
    public XMLStart asStartElement() {
        return null;
    }

    @Override
    public XMLEnd asEndElement() {
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
