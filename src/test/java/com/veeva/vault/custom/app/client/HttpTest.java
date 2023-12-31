package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.http.HttpResponseType;
import com.veeva.vault.custom.app.model.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class HttpTest {
    public static void main(String[] args) throws ProcessException {
        HttpClient client = HttpClient.newInstance();
        Map<String, String> headers = new HashMap<String, String>();
        String url = "https://api.fda.gov/drug/drugsfda.json";
        JsonObject response = client.get(url, headers, HttpResponseType.JSONDATA);
        System.out.println(response.get("meta"));
    }
}
