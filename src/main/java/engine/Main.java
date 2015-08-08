package engine;

import adapters.InputAdapter;
import adapters.OutputAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Rohan on 8/7/2015.
 */
public class Main {
    public static void main(String[] args){

        //TODO: Take input from properties/config file
        LinkedBlockingQueue<JsonArray> queue = new LinkedBlockingQueue<JsonArray>(100);

        Extractor extractor = new Extractor(queue, InputAdapter.WikiData);
        Loader loader = new Loader(queue, OutputAdapter.File);

        Thread ext = new Thread(extractor);
        Thread load = new Thread(loader);
        ext.start();
        load.start();

        try {
            ext.join();
            load.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("*** Exitting Application ***");
    }
}
