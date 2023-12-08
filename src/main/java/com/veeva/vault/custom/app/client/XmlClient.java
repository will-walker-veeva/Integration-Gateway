package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.xml.*;
import com.veeva.vault.custom.app.model.xml.XMLReader;

import java.io.*;

public class XmlClient {

    public XmlClient(){

    }

    public XMLReader readFile(com.veeva.vault.custom.app.model.files.File  file) throws Exception{
        InputStream inputStream = new FileInputStream(file.getAbsolutePath());
        return new XMLReader(inputStream);
    }


    public XMLReader readString(String xmlString) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
        return new XMLReader(inputStream);
    }

    public void closeWriter(XMLWriter writer) throws Exception{
        writer.flush();
        writer.close();
    }
}

