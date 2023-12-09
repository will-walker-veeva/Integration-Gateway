package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.xml.*;
import com.veeva.vault.custom.app.model.xml.XMLReader;

import java.io.*;

public class XmlClient {

    /**
     * @hidden
     */
    public XmlClient(){

    }

    /**
     * Creates XMLReader instance from the designated File
     * @param file The input XML file to read
     * @return An XMLReader instance of the file
     * @throws Exception
     */
    public XMLReader readFile(com.veeva.vault.custom.app.model.files.File file) throws Exception{
        InputStream inputStream = new FileInputStream(file.getAbsolutePath());
        return new XMLReader(inputStream);
    }

    /**
     * Creates XMLReader instance from XML String
     * @param xmlString An XML structured string
     * @return An XMLReader instance of the string
     * @throws Exception
     */
    public XMLReader readString(String xmlString) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
        return new XMLReader(inputStream);
    }

    /**
     * Creates an XMLWriter instance from the designated file, and input reader
     * @param file The output XML file to write to
     * @param reader The XMLReader instance the file was read from
     * @return An XMLWriter instance of the file
     * @throws Exception
     */

    public XMLWriter openWriter(com.veeva.vault.custom.app.model.files.File file, XMLReader reader) throws Exception{
        return new XMLWriter(new FileOutputStream(file.getAbsolutePath()), reader);
    }

    /**
     * Creates an XMLWriter instance from the designated file, with the given encoding, version and DTD
     * @param file The output XML file to write to
     * @param encoding The encoding of the XML file, to be written in the <?xml> tag
     * @param version The version of the XML file, to be written in the <?xml> tag
     * @param dtd The DTD of the XML file, to be written in the <!DOCTYPE> tag
     * @return An XMLWriter instance of the file
     * @throws Exception
     */

    public XMLWriter openWriter(com.veeva.vault.custom.app.model.files.File file, String encoding, String version, String dtd) throws Exception{
        return new XMLWriter(new FileOutputStream(file.getAbsolutePath()), encoding, version, dtd);
    }

}

