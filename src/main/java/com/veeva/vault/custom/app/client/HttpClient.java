package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.model.http.HttpResponseType;
import com.veeva.vault.custom.app.model.json.JsonArray;
import com.veeva.vault.custom.app.model.json.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class HttpClient{

    private static java.net.http.HttpClient newClient() throws Exception {
        return java.net.http.HttpClient.newBuilder().sslContext(SSLContext.getDefault()).build();
    }

    private static HttpRequest.Builder buildRequest(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(url));
        for(String key : headers.keySet()){
            builder.setHeader(key, headers.get(key));
        }
        return builder;
    }

    /**
     * @hidden
     */
    private HttpClient(){
        
    }

    /**
     * @hidden
     * @return
     */
    public static HttpClient newInstance(){
        return new HttpClient();
    }

    /**
     * Method for performing a GET request to the target URL with the provided headers, returning a string
     * @param url Target URL for request
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T get(String url, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).GET().build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }



    /**
     * Method for performing a POST request to the target URL with the provided headers and string body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T post(String url, String body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }

    /**
     * Method for performing a POST request to the target URL with the provided headers and form body, returning T
     * @param url Target URL for request
     * @param parameters Form body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    
    public <T> T post(String url, Map<String, String> parameters, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        String body = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return post(url, body, headers, responseType);
    }

    /**
     * Method for performing a POST request to the target URL with the provided headers and JsonObject body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return
     * @throws Exception
     */

    public <T> T post(String url, JsonObject body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception{
        return (T) post(url, body.toString(), headers, responseType);
    }

    /**
     * Method for performing a POST request to the target URL with the provided headers and byte body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T post(String url, byte[] body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }

    /**
     * Method for performing a PUT request to the target URL with the provided headers and string body, returning a string
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T put(String url, String body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }

    /**
     * Method for performing a PUT request to the target URL with the provided headers and JsonObject body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     */
    public <T> T put(String url, JsonObject body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception{
        return (T) put(url, body.toString(), headers, responseType);
    }

    /**
     * Method for performing a PUT request to the target URL with the provided headers and form body, returning T
     * @param url Target URL for request
     * @param parameters Form body
     * @param headers Headers passed with the request
     * @return Response
     */

    
    public <T> T put(String url, Map<String, String> parameters, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        String body = parameters.entrySet()
                .stream()
                .filter(e -> e.getKey()!=null)
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue()!=null? e.getValue() : "null", StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return put(url, body, headers, responseType);
    }


    /**
     * Method for performing a PUT request to the target URL with the provided headers and byte body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T put(String url, byte[] body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).PUT(HttpRequest.BodyPublishers.ofByteArray(body)).build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }

    /**
     * @hidden
     */
    private <T> T multipartPost(String url, HttpEntity entity, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        RequestBuilder reqbuilder = RequestBuilder.post(url);
        for(String key : headers.keySet()) {
            reqbuilder.addHeader(new org.apache.http.message.BasicHeader(key, headers.get(key)));
        }
        reqbuilder.setEntity(entity);
        CloseableHttpResponse response = client.execute(reqbuilder.build());
        if (HttpResponseType.STRING.equals(responseType)) {
            return (T) EntityUtils.toString(response.getEntity());
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            return (T) new JsonObject(EntityUtils.toString(response.getEntity()));
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            return (T) new JsonArray(EntityUtils.toString(response.getEntity()));
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            return (T) EntityUtils.toByteArray(response.getEntity());
        }
        return null;
    }

    /**
     * Method for performing a POST request with multipart form content type to the target URL with the provided headers and File, returning T
     * @param url Target URL for request
     * @param file File to be sent as part of the request
     * @param body Form body to be sent as part of the request
     * @param headers Headers passed with the request
     * @return Response
     */
    public <T> T multipartPost(String url, com.veeva.vault.custom.app.model.files.File file, Map<String, String> body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        File javaFile = new File(file.getAbsolutePath());
        builder.addBinaryBody("file", javaFile, ContentType.DEFAULT_BINARY, javaFile.getName());
        for(Map.Entry<String, String> entry : body.entrySet()){
            builder.addTextBody(entry.getKey(), entry.getValue());
        }
        final HttpEntity entity = builder.build();
        return multipartPost(url, entity, headers, responseType);
    }

    /**
     * Method for performing a POST request with multipart form content type to the target URL with the provided headers and File, returning T
     * @param url Target URL for request
     * @param file File to be sent as part of the request
     * @param headers Headers passed with the request
     * @return Response
     */
    public <T> T multipartPost(String url, com.veeva.vault.custom.app.model.files.File file, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        File javaFile = new File(file.getAbsolutePath());
        builder.addBinaryBody("file", javaFile, ContentType.DEFAULT_BINARY, javaFile.getName());
        final HttpEntity entity = builder.build();
        return multipartPost(url, entity, headers, responseType);
    }

    /**
     * Method for performing a DELETE request to the target URL with the provided headers, returning T
     * @param url Target URL for request
     * @param headers Headers passed with the request
     * @return Response
     */
    public <T> T delete(String url, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).DELETE().build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }

    /**
     * Method for performing a DELETE request to the target URL with the provided headers and String body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     */

    public <T> T delete(String url, String body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).method("DELETE", HttpRequest.BodyPublishers.ofString(body)).build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }

    /**
     * Method for performing a DELETE request to the target URL with the provided headers and byte body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     */
    public <T> T delete(String url, byte[] body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        HttpRequest request = buildRequest(url, headers).method("DELETE", HttpRequest.BodyPublishers.ofByteArray(body)).build();
        java.net.http.HttpClient client = newClient();
        if (HttpResponseType.STRING.equals(responseType)) {
            HttpResponse<String> strResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) strResponse.body();
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonObject(jsonResponse.body());
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            HttpResponse<String> jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (T) new JsonArray(jsonResponse.body());
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return (T) response.body();
        }
        return null;
    }


    /**
     * Method for performing a PATCH request to the target URL with the provided headers and string body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T patch(String url, String body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(url);
        StringEntity params =new StringEntity(body);
        httpPatch.setEntity(params);
        for(String key : headers.keySet()) {
            httpPatch.addHeader(key, headers.get(key));
        }
        CloseableHttpResponse response = client.execute(httpPatch);
        if (HttpResponseType.STRING.equals(responseType)) {
            return (T) EntityUtils.toString(response.getEntity());
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            return (T) new JsonObject(EntityUtils.toString(response.getEntity()));
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            return (T) new JsonArray(EntityUtils.toString(response.getEntity()));
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            return (T) EntityUtils.toByteArray(response.getEntity());
        }
        return null;
    }

    /**
     * Method for performing a PATCH request to the target URL with the provided headers and JsonObject body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     */
    public <T> T patch(String url, JsonObject body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception{
        return (T) patch(url, body.toString(), headers, responseType);
    }


    /**
     * Method for performing a PATCH request to the target URL with the provided headers and byte body, returning T
     * @param url Target URL for request
     * @param body Request body
     * @param headers Headers passed with the request
     * @return Response
     * @throws Exception
     */
    public <T> T patch(String url, byte[] body, Map<String, String> headers, HttpResponseType<T> responseType) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(url);
        ByteArrayEntity params =new ByteArrayEntity(body);
        httpPatch.setEntity(params);
        for(String key : headers.keySet()) {
            httpPatch.addHeader(key, headers.get(key));
        }
        CloseableHttpResponse response = client.execute(httpPatch);
        if (HttpResponseType.STRING.equals(responseType)) {
            return (T) EntityUtils.toString(response.getEntity());
        } else if (HttpResponseType.JSONDATA.equals(responseType)) {
            return (T) new JsonObject(EntityUtils.toString(response.getEntity()));
        } else if (HttpResponseType.JSONARRAY.equals(responseType)) {
            return (T) new JsonArray(EntityUtils.toString(response.getEntity()));
        } else if (HttpResponseType.BINARY.equals(responseType)) {
            return (T) EntityUtils.toByteArray(response.getEntity());
        }
        return null;
    }
}