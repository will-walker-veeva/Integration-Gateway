package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class CsvTest {
    @Test
    public void CsvModelTest() throws ProcessException {
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
        System.out.println(jsonString);

        MyModel model = jsonClient.deserializeObject(jsonString, MyModel.class);
        System.out.println(client.csv().serializeObjects(Arrays.asList(model)));
    }
}
