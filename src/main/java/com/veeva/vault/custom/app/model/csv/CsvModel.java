package com.veeva.vault.custom.app.model.csv;

/**
 * Interface representing a CSV serializable model
 * For example: <br>
 * <pre style="font-size: x-small;">{@code
 *     public class MyModel implements CsvModel {
 *         @CsvProperty(key = "myVal")
 *         public String myValue;
 *
 *         public JsonObject subObject;
 *
 *         @CsvProperty(key = "col2")
 *         public String someOtherValue;
 *
 *         @CsvProperty(key = "code")
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
public interface CsvModel {
}
