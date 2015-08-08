package engine;

import adapters.IInputAdapter;
import adapters.InputAdapter;
import adapters.WikiDataInputAdapter;
import com.google.gson.JsonArray;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rohan on 8/7/2015.
 */
public class Extractor implements Runnable {
    BlockingQueue<JsonArray> Queue;
    IInputAdapter inputAdapter;

    Extractor(BlockingQueue<JsonArray> queue, InputAdapter adapter){
        Queue = queue;
        if(adapter == InputAdapter.WikiData) {
            //TODO: Take input from properties/config file
            String query = "https://wdq.wmflabs.org/api?q=claim[31:11424]%20AND%20claim[364:1860]";
            inputAdapter = new WikiDataInputAdapter(10, query);
        }
    }

    public void run() {

        JsonArray batch = null;
        do {
            boolean enqueued = false;
            batch = inputAdapter.getNextBatch();

            try {
                //TODO: Take input from properties/config file
                if(batch != null) {
                    enqueued = Queue.offer(batch, 5000, TimeUnit.MILLISECONDS); //5 secs
                    if(!enqueued)
                        System.out.println("Extractor: Queue Full -- Unable to Extract -- Please Check on Loader !");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(batch!=null);

        System.out.println("*** Terminating Extractor ***");
    }
}
