package com.rtang.search;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.common.*;
import org.apache.solr.client.solrj.response.UpdateResponse;


public class IndexSolrTREC {
  final static String solrUrl = "http://localhost:8983/solr";
  final static String cloudSolrUrl = "http://localhost:9000/solr";

	private IndexSolrTREC() {}

	public static void main(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles"
				+ " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
    String indexPath = null;
		String docsPath = null;
    Boolean create = null;
    SolrClient  client = getSolrClient();
    CloudSolrClient cloudClient = getCloudSolrClient();
		for(int i=0;i<args.length;i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i+1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i+1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		if (docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {

			indexDocs(cloudClient, docDir);
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}
	}

	static void indexDocs(SolrClient client, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(client, new File(file, files[i]));
					}
				}
			} else {
				TrecSolrDocIterator docs = new TrecSolrDocIterator(file);
				SolrInputDocument doc;
				while (docs.hasNext()) {
					doc = docs.next();
					if (doc != null) {
            try {
              final UpdateResponse updateResponse = client.add("trec45", doc);
              client.commit("trec45");
            } catch (SolrServerException e) {
              System.out.println(e.toString());
            }
          }
				}
			}
		}
	}

  private static SolrClient getSolrClient() {
    return new HttpSolrClient.Builder(solrUrl)
                             .withConnectionTimeout(10000)
                             .withSocketTimeout(60000)
                             .build();
  }

  private static CloudSolrClient getCloudSolrClient() {
    return new CloudSolrClient.Builder()
                              .withSolrUrl(cloudSolrUrl)
                              .withConnectionTimeout(10000)
                              .withSocketTimeout(60000)
                              .build();
  }
}

