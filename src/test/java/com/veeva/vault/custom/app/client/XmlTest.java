package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.json.JsonObject;

public class XmlTest {
    public static void main(String[] args) throws Exception{
        Client client = new Client();
        MyModel myModel = new MyModel("Val1", "Val2", "4");
        myModel.subObject = new JsonObject();
        myModel.subObject.put("subVal", "Hello world");
        String xmlString = client.xml().serializeObject(myModel);

        MyModel regenModel = client.xml().deserializeObject(xmlString, MyModel.class);
        System.out.println(regenModel.subModel.value);
    }
}
