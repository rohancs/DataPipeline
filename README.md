# Data Pipeline

##Instructions:
    To run the program just execute Main.java
  
##Design Summary:
      1. Application offers 3 distinct stages 
        (1) Extract 
        (2) Transform -- not implemented 
        (3) Load
      2. Communication between Extrator and Loader is via a Blocking Queue 
        -- Extractor Loads the Queue & Loader empties it
      
      3. "Extractor" is responsible for extracting data from external data source 
        and enqueue it as json object for Loader to consume. Extractor may optionally instantiate 
        Transformer object (not implemented) which transforms extracted data.
      
      4. "Transformer" (not implemented) may encapsulate transformation rules that help convert extracted data 
        into format desired for Loading.
      
      5. "Loader" reads from the queue and writes to destination store.
      
      6. Both Extractor & Loader follow Adapter Pattern -- Thus Different Adapters may be implemented to extract from 
        different data sources and written out to different data stores
      
      7. This project provides "WikiDataInputAdapter" for reading WikiData and "FileOutputAdapter" to write to 
        filesystem in Json Format.
      
##Improvements Required:
      1. Resource names and run-time parameters are currently hard-coded in application. These HAVE TO BE pulled out 
      into a configuration file and passed into objects as properties - or have seperate properties files for adapters.
      2. Extraction is I/O heavy and makes several Http Calls to reterieve data. The Executor implements runnable 
      interface hence multiple threads may be used to parallelize extraction activity. Blocking Queue is thread-safe.

##Current Hard-coded Parameters
      1. Queue Size between Extractor / Loader --> 100 items
      
      ###Extractor
      2. Extraction Query -- All English Movies: 
        "https://wdq.wmflabs.org/api?q=claim[31:11424]%20AND%20claim[364:1860]"
      3. Batch Size for Single Extract/Load --> 10 records
      4. Blocking Timeout for Extractor --> 5 secs (in case Loader is unable to load fast enough) 
        and then loops until input exists
      
      ###Loader
      5. Max File Size : (999999 + 2) characters 
      6. Blocking Timeout: 10 seconds and upto 10 consequtive timeouts 
        after which Loader shuts down (Assuming there is no new data) and the application shuts down.
      7.OutputFilePath = c:\temp\data\out_<offset>.json --> Offset increments based on MaxFileSize
      
