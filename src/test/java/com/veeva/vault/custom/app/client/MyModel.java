package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.json.JsonProperty;
import com.veeva.vault.custom.app.model.json.JsonModel;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.xml.XmlAttribute;
import com.veeva.vault.custom.app.model.xml.XmlModel;
import com.veeva.vault.custom.app.model.xml.XmlModelInfo;
import com.veeva.vault.custom.app.model.xml.XmlProperty;

@XmlModelInfo(key = "thisModel")
public class MyModel implements JsonModel, XmlModel {
    @JsonProperty(key = "my_value__c", aliases = {"myOtherVal"})
    @XmlProperty(key = "valueOne", order = 1)
    public String myValue;

    @JsonProperty(key = "data")
    public JsonObject subObject;

    @XmlProperty(key = "myTag", order = 2)
    public String someOtherValue;

    @XmlAttribute(key = "code")
    public String code;

    @XmlProperty(key = "holder")
    public SubModel subModel;

    public MyModel(){

    }

    public MyModel(String myValue, String someOtherValue, String code){
        this.myValue = myValue;
        this.someOtherValue = someOtherValue;
        this.code = code;
        this.subModel = new SubModel();
    }

    @XmlModelInfo()
    public static class SubModel implements XmlModel{
        @XmlAttribute(key = "code")
        public String code;

        @XmlProperty()
        public String value;

        public SubModel(){
            this.code="1";
            this.value="text";
        }
    }
}