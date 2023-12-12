package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class WebUtils {

    public static JsonObject generateChat(String key, String nickname) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("key", key);
        content.addProperty("nickname", nickname);

        System.out.println(content);

        return sendPostRequest("https://xeol.online/anonchat-create", content.toString());
    }

    public static String fetchMessage(String key, String message) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("key", key);
        content.addProperty("message", message);

        JsonObject result = sendPostRequest("https://xeol.online/anonchat-fetch-message", content.toString());

        return result.get("realMessage").getAsString();

    }

    public static JsonObject createMessage(String key, String message, String fakeMessage) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("key", key);
        content.addProperty("fakeMessage", fakeMessage);
        content.addProperty("message", message);

        return sendPostRequest("https://xeol.online/anonchat-create-message", content.toString());
    }

    public static String getRandomQuote() throws IOException {

        try {
           JsonArray result = sendGetRequest("https://api.quotable.io/quotes/random");
            return result.get(0).getAsJsonObject().get("content").getAsString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static JsonArray sendGetRequest(String requestUrl) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(requestUrl);

        CloseableHttpResponse response = httpClient.execute(httpGet);

        String responseBody = EntityUtils.toString(response.getEntity());

        return JsonParser.parseString(responseBody).getAsJsonArray();
    }

    public static JsonObject sendPostRequest(String requestUrl, String content) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(requestUrl);

        httpPost.setHeader("Content-Type", "application/json");

        httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));

        CloseableHttpResponse response = httpClient.execute(httpPost);

        String responseBody = EntityUtils.toString(response.getEntity());

        return JsonParser.parseString(responseBody).getAsJsonObject();
    }


}
