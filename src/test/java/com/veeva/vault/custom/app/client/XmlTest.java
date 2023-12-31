package com.veeva.vault.custom.app.client;


import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;

import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class XmlTest {
    public static void main(String[] args) throws ProcessException {
        Client client = new Client();
        MyModel myModel = new MyModel("Val1", "Val2", "4");
        myModel.subObject = new JsonObject();
        myModel.subObject.put("subVal", "Hello world");
        myModel.subModel = new MyModel.SubModel();
        myModel.subModel.id = java.util.UUID.randomUUID().toString();
        myModel.subModels = IntStream.range(0, 5).mapToObj(el -> new MyModel.SubModel(java.util.UUID.randomUUID().toString())).collect(Collectors.toList());
        String xmlString = client.xml().serializeObject(myModel);
        System.out.println(xmlString);
        MyModel regenModel = client.xml().deserializeObject(xmlString, MyModel.class);
        System.out.println(regenModel.code);
    }

}
