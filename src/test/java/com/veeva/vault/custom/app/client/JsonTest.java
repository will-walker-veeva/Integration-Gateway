package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    public void JsonModelTest() throws ProcessException {
        Client client = new Client();
        JsonClient jsonClient = client.json();
        JsonObject subObject = new JsonObject();
        subObject.put("totalresults", 0);
        subObject.put("objectName", "my_object__c");

        JsonObject object = new JsonObject();
        object.put("myOtherVal", "12345");
        object.put("hi", "12345");
        object.put("data", subObject);
        String jsonString = object.toString();
        //System.out.println(object);

        MyModel model = jsonClient.deserializeObject(jsonString, MyModel.class);
        //System.out.println(model.myValue);
        //System.out.println(model.subObject);
        System.out.println(jsonClient.serializeObject(model));
    }
}
