#### Search Engine Demo (CS291A)

This Maven project uses Solr to index the TREC45 dataset in XML format. You can query the dataset through a simple webservice built on SparkJava that will highlight words within your query in the response.

##### How to run/test
1. `mvn clean compile`
2. Start a Solr instance by downloading the Solr package [here](http://www.apache.org/dyn/closer.lua/lucene/solr/7.2.1) and running `bin/solr start` on port `8983`
3. Now that your Solr server is running, create a collection/core named `trec45` to which we can index
4. After you have created the collection, we can index a sample dataset by running `java -cp target/search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar com.rtang.search.IndexSolrTREC -docs /path/to/sample-dataset.xml`
4. Check that the dataset has been successfully indexed by checking at `localhost:8983/solr`
5. If it has been successfully indexed, we can start our web service which will allow us to query the dataset by running `java -jar target/search-engine-1.0-SNAPSHOT-jar-with-dependencies.ja`
6. The webservice will be located at `localhost:4567`. Happy searching!
