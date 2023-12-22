package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.JsonClient;
import com.veeva.vault.custom.app.model.json.JsonElement;
import com.veeva.vault.custom.app.model.json.JsonModel;
import com.veeva.vault.custom.app.model.json.JsonObject;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    public void JsonModelTest() throws Exception{
        Client client = new Client();
        JsonClient jsonClient = client.json();
        JsonObject subObject = new JsonObject();
        subObject.put("totalresults", 0);
        subObject.put("objectName", "my_object__c");

        JsonObject object = new JsonObject();
        object.put("my_value__c", "12345");
        object.put("hi", "12345");
        object.put("data", subObject);
        String jsonString = object.toString();
        System.out.println(object);

        MyModel model = jsonClient.serializeObject(jsonString, MyModel.class);
        System.out.println(model.myValue);
        System.out.println(model.subObject);
        System.out.println(jsonClient.deserializeObject(model));
    }

    public static class MyModel implements JsonModel{
        @JsonElement(key = "my_value__c")
        String myValue;

        @JsonElement(key = "data")
        JsonObject subObject;

        String someOtherValue;

        public MyModel(){

        }
    }
}
