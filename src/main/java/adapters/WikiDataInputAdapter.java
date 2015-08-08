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

/**
 * Created by Rohan on 8/7/2015.
 */
public class WikiDataInputAdapter implements IInputAdapter {
    int BatchSize,CurrentPointer;
    String query;
    JsonArray items = null;

    public WikiDataInputAdapter(int batchSize,String Query){
        BatchSize = batchSize;
        query = Query;
        bootstrap();
        CurrentPointer=0;
    }

    public JsonArray getNextBatch() {
        JsonArray result = new JsonArray();

        for (int batchIndex=0;CurrentPointer < items.size() && batchIndex < BatchSize;batchIndex++, CurrentPointer++)
            result.add(getRecord(items.get(CurrentPointer)));

        if(result.size() == 0)
            return null;

        System.out.println("Processing Batch "+(CurrentPointer/BatchSize)+" of "+(items.size() / BatchSize));
        return result;
    }

    private JsonObject getRecord(JsonElement key){
        JsonObject record = new JsonObject();
        JsonObject base = getWikiDataJson(key);

        //Title
        String title = base.getAsJsonObject("labels").getAsJsonObject("en").getAsJsonPrimitive("value").getAsString();
        record.addProperty("title",title);

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

        if(Property.equals("P921"))
            System.out.print("");
        try {
            JsonArray items = base.getAsJsonObject("claims").getAsJsonArray(Property);
            for (JsonElement e : items) {
                JsonElement genreKey = e.getAsJsonObject().getAsJsonObject("mainsnak")
                        .getAsJsonObject("datavalue")
                        .getAsJsonObject("value")
                        .getAsJsonPrimitive("numeric-id");
                JsonObject genreResponse = getWikiDataJson(genreKey);

                result.add(genreResponse.getAsJsonObject("labels").getAsJsonObject("en").getAsJsonPrimitive("value"));
            }
        }
        catch (Exception e){
            //Log if needed -- value does not exist on record
        }

        return result;
    }

    private JsonObject getWikiDataJson(JsonElement key){
        String response = _HttpGet("https://www.wikidata.org/wiki/Special:EntityData/Q" + key.getAsString() + ".json");
        JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
        return responseJson.getAsJsonObject("entities").getAsJsonObject("Q" + key.getAsString());
    }

    private String _HttpGet(String Url){
        JerseyClient client = JerseyClientBuilder.createClient();
        WebTarget resource = client.target(Url);

        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON);

        return request.get(String.class);
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
