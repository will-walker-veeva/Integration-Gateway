package com.veeva.vault.custom.app.model.http;

import com.veeva.vault.custom.app.model.json.JsonArray;
import com.veeva.vault.custom.app.model.json.JsonObject;

public class HttpResponseType<T> {
    public static final HttpResponseType<byte[]> BINARY = new HttpResponseType<byte[]>(byte[].class);
    public static final HttpResponseType<JsonObject> JSONDATA = new HttpResponseType<JsonObject>(JsonObject.class);
    public static final HttpResponseType<JsonArray> JSONARRAY = new HttpResponseType<JsonArray>(JsonArray.class);
    public static final HttpResponseType<String> STRING = new HttpResponseType<String>(String.class);

    private Class<T> className;

    private HttpResponseType(Class<T> className){
        this.className = className;
    }
}
