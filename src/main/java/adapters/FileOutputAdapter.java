package adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;

/**
 * Created by Rohan on 8/7/2015.
 */
public class FileOutputAdapter implements IOutputAdapter {
    String filepath,filePattern;
    FileWriter fout;
    File currentFile;
    int currentFileSize,maxFileSize;
    Integer offset=0;

    public FileOutputAdapter(int MaxFileSize){
        maxFileSize = MaxFileSize;
        currentFileSize = maxFileSize +1; //Only at start up -- to force file creation
        if(maxFileSize < 10) maxFileSize = 10;

        //TODO: Take input from properties/config file
        filepath ="c:\\temp\\data";
        filePattern = "out_{}.json";

    }

    void newFile(){
        File file = new File(filepath);
        if(!file.exists()) file.mkdirs();

        try {
            currentFile = new File(filepath+"\\"+filePattern.replace("{}",offset.toString()));
            currentFile.createNewFile();
            currentFileSize=0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean saveBatch(JsonArray batch) {
        try {
            if (currentFileSize > maxFileSize) {
                if(currentFile!=null && currentFile.exists()){
                    fout.append("]");
                    fout.flush();
                    fout.close();
                }

                newFile();
                fout = new FileWriter(currentFile);
                fout.append("[");
                currentFileSize = 2; //For '[' & ']'
                offset++;
            }

          if(currentFileSize > 2)fout.append(",");
          for(int i=0; i<batch.size();i++) {
              String data = batch.get(i).toString();

              fout.append(data);
              if (i < batch.size() - 1) fout.append(",");
              currentFileSize += data.length();
          }
           fout.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            fout.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
