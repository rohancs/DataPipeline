package adapters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.client.InvocationCallback;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Rohan on 8/8/2015.
 */
public class InvocationCallbackHelper implements InvocationCallback<String> {
    LinkedBlockingQueue<JsonObject> buffer;
    InvocationCallbackHelper(LinkedBlockingQueue<JsonObject> Buffer){
        buffer = Buffer;
    }

    public void completed(String s) {
        JsonObject response = new JsonParser().parse(s).getAsJsonObject();
        JsonObject base = response.getAsJsonObject("entities").entrySet().iterator().next().getValue().getAsJsonObject();
        buffer.add(base);
    }

    public void failed(Throwable throwable) {
        buffer.add(new JsonObject());
    }
}
