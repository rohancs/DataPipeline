package engine;

import adapters.*;
import com.google.gson.JsonArray;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rohan on 8/7/2015.
 */
public class Loader implements Runnable {
    BlockingQueue<JsonArray> Queue;
    IOutputAdapter outputAdapter;

    Loader(BlockingQueue<JsonArray> queue, OutputAdapter adapter){
        Queue = queue;
        if(adapter == OutputAdapter.File) {
            //TODO: Take input from properties/config file
            outputAdapter = new FileOutputAdapter(5000);
        }
    }
    public void run() {

        boolean timeout = false;
        int threshold = 0,totalTimeouts=10;
        do {
            JsonArray batch = null;
            try {
                //TODO: Take input from properties/config file
                batch = Queue.poll(10000, TimeUnit.MILLISECONDS); //10 secs
                if(batch != null)
                outputAdapter.saveBatch(batch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(batch == null) {
                System.out.println("Loader: Queue Empty -- Check on Extractor -- Timeout#:" + (threshold + 1)+" of "+totalTimeouts);
                threshold++;
            }
            else
                threshold=0; //Reset Counter

            //TODO: Take input from properties/config file
            if(threshold > 10) timeout=true;

        }while (!timeout);

        System.out.println("*** Terminating Loader ***");
    }

}
