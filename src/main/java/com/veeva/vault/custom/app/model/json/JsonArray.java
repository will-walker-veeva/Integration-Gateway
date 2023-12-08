package com.veeva.vault.custom.app.model.json;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonArray{
    private JSONArray jsonArray = new JSONArray();
    public JsonArray(){
    }

    public JsonArray(String jsonArrayStr){
        jsonArray = new JSONArray(jsonArrayStr);
    }

    public JsonObject getJsonObject(Integer i){
        return new JsonObject(jsonArray.getJSONObject(i).toString());
    }

    public JsonArray getJsonArray(Integer i){
        return new JsonArray(jsonArray.getJSONArray(i).toString());
    }

    public void put(JsonObject jsonObject){
        this.jsonArray.put(new JSONObject(jsonObject.toString()));
    }

    public void put(JsonArray jsonArray){
        this.jsonArray.put(new JSONArray(jsonArray.toString()));
    }

    public void put(Object object){
        this.jsonArray.put(object);
    }

    public void put(Integer object){
        this.jsonArray.put(object);
    }

    public void put(Long object){
        this.jsonArray.put(object);
    }

    public void put(Float object){
        this.jsonArray.put(object);
    }

    public void put(Double object){
        this.jsonArray.put(object);
    }

    public void put(Boolean object){
        this.jsonArray.put(object);
    }

    public void put(String object){
        this.jsonArray.put(object);
    }

    public Long getLong(int index){
        return this.jsonArray.optLong(index);
    }

    public Integer getInt(int index){
        return this.jsonArray.optInt(index);
    }

    public Double getDouble(int index){
        return this.jsonArray.optDouble(index);
    }

    public Float getFloat(int index){
        return this.jsonArray.optFloat(index);
    }

    public String getString(int index){
        return this.jsonArray.optString(index);
    }

    public Boolean getBoolean(int index){
        return this.jsonArray.optBoolean(index);
    }

    public Object get(int index){
        return this.jsonArray.opt(index);
    }

    @Override
    public String toString(){
        return this.jsonArray.toString();
    }

}
