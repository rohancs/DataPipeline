# Data Pipeline

##Instructions:
    To run the program just execute Main.java
  
##Design Summary:
* Application offers 3 distinct stages 
    (1) Extract 
    (2) Transform -- not implemented 
    (3) Load

* Communication between Extractor and Loader is via a Blocking Queue 
    -- "Extractor" Loads the Queue & "Loader" empties it
      
* "Extractor" is responsible for extracting data from external data source 
    and enqueue it as json object for Loader to consume. Extractor may optionally instantiate 
    Transformer object (not implemented) which transforms extracted data.
      
* "Transformer" (not implemented) may encapsulate transformation rules that help 
    convert extracted data into format desired for Loading.
      
* "Loader" reads from the queue and writes to destination store.
      
* Application starts with one thread running "Extractor" and one thread running "Loader" and passes them a common queue object to communicate.

* Both Extractor & Loader use Adapter Pattern -- Thus Different Adapters may be 
    implemented to extract data from different data sources and written out to different data stores
      
* This project provides "WikiDataInputAdapter" for reading WikiData and "FileOutputAdapter" 
    to write to filesystem in Json Format.
      
##Improvements Required:
* Resource names and run-time parameters are currently hard-coded in application. 
  These HAVE TO BE pulled out into a configuration file and passed into objects as properties
  or have seperate properties files for adapters.
      
* Extraction is I/O heavy and makes several Http Calls to reterieve data. 
  The Executor implements runnable interface hence multiple threads (with smaller exclusive queries) 
  may be used to parallelize extraction activity. Blocking Queue is thread-safe thus this design works.
  Local Optimizations have been made to make all Movie Requests in a batch in async mode, All Item de-references within a movie is also done asynchronously.

##Current Hard-coded Parameters
* Queue Size between Extractor / Loader ==> 100 items
      
Extractor:

* Extraction Query -- All English Movies: 
    "https://wdq.wmflabs.org/api?q=claim[31:11424]%20AND%20claim[364:1860]"

* Batch Size for Single Extract/Load --> 10 records

* Blocking Timeout for Extractor --> 5 secs (in case Loader is unable to load fast enough) 
        and then loops until input exists
      
Loader:

* Max File Size : (99999 + 2) characters 
* Blocking Timeout: 10 seconds and upto 10 consequtive timeouts 
    after which Loader shuts down (Assuming there is no new data) and the application shuts down.
* OutputFilePath = c:\temp\data\out_<offset>.json --> Offset increments based on MaxFileSize
      
##Depends On
* Google GSON library for working with json documents
* Jersey client library to make Http service requests
