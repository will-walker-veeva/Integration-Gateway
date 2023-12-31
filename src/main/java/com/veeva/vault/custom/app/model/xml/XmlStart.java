package com.veeva.vault.custom.app.model.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Namespace;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XmlStart implements XmlElement {
    private String xPath;
    private String name;
    private Map<String, String> attributes;
    private Iterator<Namespace> namespaces = null;
    private QName qname = null;

    public XmlStart(String name){
        this.name = name;
    }

    public XmlStart(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public XmlStart(String xPath, String name, Map<String, String> attributes) {
        this.xPath = xPath;
        this.name = name;
        this.attributes = attributes;
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
        return true;
    }

    @Override
    public boolean isEndElement() {
        return false;
    }

    @Override
    public XmlComment asComment() {
        return null;
    }

    @Override
    public XmlStart asStartElement() {
        return this;
    }

    @Override
    public XmlEnd asEndElement() {
        return null;
    }

    public String getName(){
        return name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttribute(String attributeKey, String attributeValue){
        if(this.attributes==null) this.attributes = new HashMap<String, String>();
        this.attributes.put(attributeKey, attributeValue);
    }

    protected void setNamespaces(Iterator<Namespace> namespaces){
        this.namespaces = namespaces;
    }

    protected Iterator<Namespace> getNamespaces(){
        return this.namespaces;
    }

    protected void setQName(QName qname){
        this.qname = qname;
    }

    protected QName getQName(){
        return this.qname;
    }
}
