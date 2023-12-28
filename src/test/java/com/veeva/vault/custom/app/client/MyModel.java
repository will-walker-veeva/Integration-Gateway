package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.json.JsonElement;
import com.veeva.vault.custom.app.model.json.JsonModel;
import com.veeva.vault.custom.app.model.json.JsonObject;

public class MyModel implements JsonModel {
    @JsonElement(key = "my_value__c")
    String myValue;

    @JsonElement(key = "data")
    JsonObject subObject;

    String someOtherValue;

    public MyModel(){

    }
}