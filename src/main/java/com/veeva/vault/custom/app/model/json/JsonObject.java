package com.veeva.vault.custom.app.model.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import org.json.JSONArray;
import org.json.JSONObject;
public class JsonObject {
    private JSONObject jsonObject = new JSONObject();
    public JsonObject(){

    }

    public JsonObject(String jsonString){
        jsonObject = new JSONObject(jsonString);
    }

    public JsonObject getJsonObject(String key){
        return new JsonObject(this.jsonObject.getJSONObject(key).toString());
    }

    public JsonArray getJsonArray(String key){
        return new JsonArray(this.jsonObject.getJSONArray(key).toString());
    }

    public void put(String key, JsonObject jsonObject){
        this.jsonObject.put(key, new JSONObject(jsonObject.toString()));
    }

    public void put(String key, JsonArray jsonArray){
        this.jsonObject.put(key, new JSONArray(jsonArray.toString()));
    }

    public void put(String key, Object object){
        this.jsonObject.put(key, object);
    }

    public void put(String key, Integer object){
        this.jsonObject.put(key, object);
    }

    public void put(String key, Long object){
        this.jsonObject.put(key, object);
    }

    public void put(String key, Float object){
        this.jsonObject.put(key, object);
    }

    public void put(String key, Double object){
        this.jsonObject.put(key, object);
    }

    public void put(String key, Boolean object){
        this.jsonObject.put(key, object);
    }

    @JsonAnySetter
    public void put(String key, String object){
        this.jsonObject.put(key, object);
    }

    public Long getLong(String key){
        return this.jsonObject.optLong(key);
    }

    public Integer getInt(String key){
        return this.jsonObject.optInt(key);
    }

    public Double getDouble(String key){
        return this.jsonObject.optDouble(key);
    }

    public Float getFloat(String key){
        return this.jsonObject.optFloat(key);
    }

    public String getString(String key){
        return this.jsonObject.optString(key);
    }

    public Boolean getBoolean(String key){
        return this.jsonObject.optBoolean(key);
    }

    public Object get(String key){
        return this.jsonObject.opt(key);
    }

    @Override
    public String toString(){
        return this.jsonObject.toString();
    }
}
