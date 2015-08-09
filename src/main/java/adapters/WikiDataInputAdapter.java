package adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rohan on 8/7/2015.
 */
public class WikiDataInputAdapter implements IInputAdapter {
    int BatchSize,CurrentPointer;
    String query;
    JsonArray items = null;
    HashMap<String,JsonElement> properties;


    public WikiDataInputAdapter(int batchSize,String Query){
        BatchSize = batchSize;
        query = Query;
        bootstrap();
        CurrentPointer=0;
        properties = new HashMap<String,JsonElement>();
    }

    public JsonArray getNextBatch() {
        JsonArray result = new JsonArray();
        LinkedBlockingQueue<JsonObject> buffer = new LinkedBlockingQueue<JsonObject>();
        int batchIndex=0;

        //Async Requests to get Movie Object
        for (;CurrentPointer < items.size() && batchIndex < BatchSize;batchIndex++, CurrentPointer++)
            getWikiDataJson(items.get(CurrentPointer),true,buffer);

        //Process Responses
        while (batchIndex != 0){
            try {
                JsonObject response = buffer.poll(5000, TimeUnit.MILLISECONDS);
                if(response != null) {
                    JsonObject record = getRecord(response);
                    if(record != null) result.add(record); //Ignore Bad Data
                    batchIndex--;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(result.size() == 0)
            return null;

        System.out.println("Processing Batch "+(CurrentPointer/BatchSize)+" of "+(items.size() / BatchSize));
        return result;
    }

    private JsonObject getRecord(JsonObject base){
        JsonObject record = new JsonObject();
        //Title
        try {
          String title = base.getAsJsonObject("labels").getAsJsonObject("en").getAsJsonPrimitive("value").getAsString();
          record.addProperty("title",title);
        }
        catch (Exception e){
            //If there is No Title -- Return Null
            System.out.println("Exception Occured in :"+base.toString());
            return null;
        }

        //Genre:P136
        record.add("genres",getItems(base,"P136"));

        //Main Subjects:P921
        record.add("mainSubjects",getItems(base,"P921"));

        //Cast (Actors):P161
        record.add("cast",getItems(base,"P161"));

        return record;
    }

    private JsonArray getItems(JsonObject base,String Property){
        JsonArray result = new JsonArray();

        try {
            LinkedBlockingQueue<JsonObject> buffer = new LinkedBlockingQueue<JsonObject>();
            int asyncRequestCounter=0;
            JsonArray items = base.getAsJsonObject("claims").getAsJsonArray(Property);

            for (JsonElement e : items) {
                JsonElement genreKey = e.getAsJsonObject().getAsJsonObject("mainsnak")
                        .getAsJsonObject("datavalue")
                        .getAsJsonObject("value")
                        .getAsJsonPrimitive("numeric-id");

                JsonObject genreResponse = null;

                //Lookup Cache -- or else -- Make Async Request
                if(properties.containsKey(genreKey))
                    result.add(properties.get(genreKey));
                else {
                    getWikiDataJson(genreKey, true, buffer); //Asyc Request
                    asyncRequestCounter++;
                }
            }

            //Process Async Responses
            while (asyncRequestCounter != 0){
                try {
                    JsonObject response = buffer.poll(5000, TimeUnit.MILLISECONDS);
                    if(response != null) {
                        JsonElement Value = response.getAsJsonObject("labels").getAsJsonObject("en").getAsJsonPrimitive("value");
                        result.add(Value);
                        properties.put(response.get("id").getAsString().replace("Q",""),Value);
                        asyncRequestCounter--;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){
            //Log if needed -- value does not exist on record
        }

        return result;
    }

    private JsonObject getWikiDataJson(JsonElement key){
        //Synchronous API Call
        return getWikiDataJson(key,false,null);
    }

    private JsonObject getWikiDataJson(JsonElement key,boolean async,LinkedBlockingQueue<JsonObject> buffer){
        String request = "https://www.wikidata.org/wiki/Special:EntityData/Q" + key.getAsString() + ".json";
        String response = null;
        if(!async) {
            response = _HttpGet(request);
            JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
            return responseJson.getAsJsonObject("entities").getAsJsonObject("Q" + key.getAsString());
        }
        else{
            _HttpGetAsync(request,buffer);
        }

        return null;
    }

    private String _HttpGet(String Url){
        //Synchronous API Call
        JerseyClient client = JerseyClientBuilder.createClient();
        WebTarget resource = client.target(Url);

        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON);

        return request.get(String.class);
    }

    private void _HttpGetAsync(String Url,LinkedBlockingQueue<JsonObject> buffer){
        JerseyClient client = JerseyClientBuilder.createClient();
        WebTarget resource = client.target(Url);
        resource.request().async().get(new InvocationCallbackHelper(buffer));
    }

    private void bootstrap() {
        String response = _HttpGet(query);
        items = new com.google.gson.JsonParser()
                        .parse(response)
                        .getAsJsonObject()
                        .get("items")
                        .getAsJsonArray();
    }
}
