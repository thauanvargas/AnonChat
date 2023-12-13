package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import extension.AnonChat;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

public class WebUtils {

    private static final TreeMap<String, String> codeToLangMap = new TreeMap<>();
    static {
        codeToLangMap.put("br", "pt-br");
        codeToLangMap.put("de", "de");
        codeToLangMap.put("es", "es");
        codeToLangMap.put("fi", "fi");
        codeToLangMap.put("fr", "fr");
        codeToLangMap.put("it", "it");
        codeToLangMap.put("nl", "nl");
        codeToLangMap.put("tr", "tr");
        codeToLangMap.put("en", "com");
    }

    public static JsonObject generateChat(String key, String index, int room) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("key", key);
        content.addProperty("room", room);
        content.addProperty("index", index);

        return sendPostRequest("https://xeol.online/anonchat-create", content.toString());
    }

    public static String fetchMessage(String key, String message, int room) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("key", key);
        content.addProperty("message", message);
        content.addProperty("room", room);

        JsonObject result = sendPostRequest("https://xeol.online/anonchat-fetch-message", content.toString());

        return result.get("realMessage").getAsString();

    }

    public static JsonObject createMessage(String key, String message, String fakeMessage, int room) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("key", key);
        content.addProperty("room", room);
        content.addProperty("fakeMessage", fakeMessage);
        content.addProperty("message", message);

        return sendPostRequest("https://xeol.online/anonchat-create-message", content.toString());
    }

    public static String getRandomQuote(int minLength, int maxLength) throws IOException {

        JsonObject content = new JsonObject();

        content.addProperty("lang", codeToLangMap.get(AnonChat.RUNNING_INSTANCE.host));
        content.addProperty("minLength", minLength);
        content.addProperty("maxLength", maxLength);

        JsonObject response = sendPostRequest("https://xeol.online/anonchat-get-fake-message", content.toString());

        return response.get("content").getAsString();

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
