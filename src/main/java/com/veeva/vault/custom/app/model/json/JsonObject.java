package com.veeva.vault.custom.app.model.json;

import com.veeva.vault.custom.app.model.core.AnyGetter;
import com.veeva.vault.custom.app.model.core.AnySetter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class JsonObject implements Map<String, Object> {
    @AnyGetter
    @AnySetter
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

    @Override
    public int size() {
        return this.jsonObject.toMap().size();
    }

    @Override
    public boolean isEmpty() {
        return this.jsonObject.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.jsonObject.has((String)key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.jsonObject.toMap().containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this.jsonObject.get((String) key);
    }

    public Object put(String key, Object object){
        this.jsonObject.put(key, object);
        return object;
    }

    @Override
    public Object remove(Object key) {
        return this.jsonObject.remove((String)key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        m.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void clear() {
        this.jsonObject.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return this.jsonObject.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return this.jsonObject.toMap().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.jsonObject.toMap().entrySet();
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
