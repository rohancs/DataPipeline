package adapters;

import com.google.gson.JsonArray;

/**
 * Created by Rohan on 8/7/2015.
 */
public interface IOutputAdapter {
    boolean saveBatch(JsonArray batch);
}
