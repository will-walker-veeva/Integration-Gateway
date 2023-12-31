package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.csv.CsvModel;
import com.veeva.vault.custom.app.model.csv.CsvProperty;
import com.veeva.vault.custom.app.model.json.JsonProperty;
import com.veeva.vault.custom.app.model.json.JsonModel;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.xml.XmlAttribute;
import com.veeva.vault.custom.app.model.xml.XmlModel;
import com.veeva.vault.custom.app.model.xml.XmlModelInfo;
import com.veeva.vault.custom.app.model.xml.XmlProperty;

import java.util.List;

@XmlModelInfo(key = "thisModel")
public class MyModel implements JsonModel, XmlModel, CsvModel {
    @JsonProperty(key = "my_value__c", aliases = {"myOtherVal"})
    @XmlProperty(key = "valueOne", order = 1)
    @CsvProperty(key = "myVal")
    public String myValue;

    @JsonProperty(key = "data")
    public JsonObject subObject;

    @XmlProperty(key = "myTag", order = 2)
    @CsvProperty(key = "myTag")
    public String someOtherValue;

    @XmlAttribute(key = "code")
    @CsvProperty(key = "code")
    public String code;

    @XmlProperty(key = "holder")
    public SubModel subModel;

    @XmlProperty(key = "drug")
    @JsonProperty(key = "drug")
    public List<SubModel> subModels;

    public MyModel(){

    }

    public MyModel(String myValue, String someOtherValue, String code){
        this.myValue = myValue;
        this.someOtherValue = someOtherValue;
        this.code = code;
        //this.subModel = new SubModel();
        //this.subModels = IntStream.range(0, 5).mapToObj(el -> new SubModel()).collect(Collectors.toList());
    }

    @XmlModelInfo()
    public static class SubModel implements XmlModel, JsonModel, CsvModel{
        @XmlAttribute(key = "code")
        @JsonProperty(key = "code")
        public String code;

        @XmlProperty(key = "someVal")
        public String value;

        @JsonProperty(key = "id")
        public String id;

        public SubModel(){
            //this.code="1";
            //this.value="text";
            //this.id = java.util.UUID.randomUUID().toString();
        }

        public SubModel(String id){
            this.id = id;
        }
    }
}