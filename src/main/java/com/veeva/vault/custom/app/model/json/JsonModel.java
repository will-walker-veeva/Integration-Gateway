package com.veeva.vault.custom.app.model.json;

/**
 * Interface representing a JSON serializable model
 * For example: <br>
 * <pre style="font-size: x-small;">{@code
 *     public class MyModel implements JsonModel {
 *         @JsonProperty(key = "myVal")
 *         public String myValue;
 *
 *         @AnyGetter
 *         @AnySetter
 *         public JsonObject subObject;
 *
 *         @JsonProperty(key = "val2")
 *         public String someOtherValue;
 *
 *         @JsonProperty(key = "code")
 *         public String code;
 *
 *         public List<SubModel> subModels;
 *
 *         //All models require a default no-argument constructor
 *         public MyModel(){
 *
 *         }
 *     }
 * }</pre>
 */
public interface JsonModel {
}
