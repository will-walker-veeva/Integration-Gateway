package com.veeva.vault.custom.app.model.json;

import com.veeva.vault.custom.app.model.core.AnyGetter;
import com.veeva.vault.custom.app.model.core.AnySetter;
import org.jetbrains.annotations.NotNull;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a JSON array.
 */
public class JsonArray implements Collection<Object>, Iterable<Object>{
    @AnySetter
    @AnyGetter
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

    @Override
    public int size() {
        return this.jsonArray.length();
    }

    @Override
    public boolean isEmpty() {
        return this.jsonArray.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.jsonArray.toList().contains(o);
    }

    @NotNull
    @Override
    public Iterator<Object> iterator() {
        return this.jsonArray.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.jsonArray.toList().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return this.jsonArray.toList().toArray(a);
    }

    @Override
    public boolean add(Object o) {
        this.jsonArray.put(o);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        this.jsonArray.remove((Integer) o);
        return true;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.jsonArray.toList().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(el -> add(el));
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(el -> remove(el));
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        List<Object> elements = this.jsonArray.toList();
        boolean retainAll = elements.retainAll(c);
        this.jsonArray = new JSONArray(elements);
        return retainAll;
    }

    @Override
    public void clear() {
        this.jsonArray.clear();
    }

    public static JsonArray ofCsvString(String csv){
        JsonArray jsonArray = new JsonArray();
        jsonArray.jsonArray = CDL.toJSONArray(csv);
        return jsonArray;
    }

    public String toCsvString(){
        return CDL.toString(this.jsonArray);
    }
}
