package com.veeva.vault.custom.app.model.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Namespace;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents an XML Start Element
 */
public class XmlStart implements XmlElement {
    private String xPath;
    private String name;
    private Map<String, String> attributes;
    private Iterator<Namespace> namespaces = null;
    private QName qname = null;

    /**
     * Construct an XML Start with the given name
     * @param name
     */
    public XmlStart(String name){
        this.name = name;
    }

    /**
     * Construct an XML Start with the given name and attributes
     * @param name
     * @param attributes
     */
    public XmlStart(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    /**
     * @hidden
     * @param xPath
     * @param name
     * @param attributes
     */
    public XmlStart(String xPath, String name, Map<String, String> attributes) {
        this.xPath = xPath;
        this.name = name;
        this.attributes = attributes;
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
        return true;
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
        return null;
    }

    /**
     * Returns element as XmlStart if the XmlElement is a {@link XmlStart}
     * @return
     */
    @Override
    public XmlStart asStartElement() {
        return this;
    }

    /**
     * Returns element as XmlEnd if the XmlElement is a {@link XmlEnd}
     * @return
     */
    @Override
    public XmlEnd asEndElement() {
        return null;
    }

    /**
     * Get the name of this element
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * Returns a java.util.Map of Attributes
     * @return
     */
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
