package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.query.QueryModel;
import com.veeva.vault.custom.app.model.query.QueryModelInfo;
import com.veeva.vault.custom.app.model.query.QueryProperty;
import com.veeva.vault.custom.app.model.query.QueryPropertyType;


@QueryModelInfo(name = "my_table")
public class MyQueryModel extends QueryModel {
    @QueryProperty(key  = "firstName", type = QueryPropertyType.TEXT)
    private String firstName;
    @QueryProperty(key  = "lastName", type = QueryPropertyType.TEXT)
    private String lastName;

    public MyQueryModel(String firstName, String lastName) {
        super(null);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString(){
        JsonObject obj = new JsonObject();
        obj.put("fistName", firstName);
        obj.put("lastName", lastName);
        return obj.toString();
    }

    public static void main(String[] args){
        MyQueryModel model = new MyQueryModel("John", "Smith");
        System.out.println(model.toString());
    }
}
