package com.rtang.search;

import java.util.*;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.common.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.MapSolrParams;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static spark.Spark.*;


public class App {
  final static String solrUrl = "http://localhost:8983/solr";
  final static String cloudSolrUrl = "http://localhost:9000/solr";

  public static void main(String[] args) {
    final SolrClient client = getSolrClient();
    final CloudSolrClient cloudClient = getCloudSolrClient();
    Pattern headline_tag = Pattern.compile("<HEADLINE>(.+?)</HEADLINE>"); Pattern profile_tag = Pattern.compile("<PROFILE>(.+?)</PROFILE>");
    Pattern byline_tag = Pattern.compile("<BYLINE>(.+?)</BYLINE>");
    Pattern text_tag = Pattern.compile("<TEXT>(.+?)</TEXT>");

    get("/", (request, response) -> {
        String search_new = "<div style='margin:auto;float:center;text-align:center;'><form action='/' method='POST'><input class='form-control' id='query_trec' name='query_trec' placeholder='' type='text' value='' style='width:700px;height:50px;font-size:14pt;margin:auto;'><br><input id='btn_query' name='btn_query' type='submit' class='btn btn-default' value='Query' style='width:500px;font-size:14pt;margin:auto;'></form></div>";
        String search = "<form action='/' method='POST'><input class='form-control' id='query_trec' name='query_trec' placeholder='' type='text' value='' /><input id='btn_query' name='btn_query' type='submit' class='btn btn-default' value='Query' /></form></br>";
        return search_new;
        });

    post("/", (request, response) -> {
        String search_new = "<div style='margin:auto;float:center;text-align:center;'><form action='/' method='POST'><input class='form-control' id='query_trec' name='query_trec' placeholder='' type='text' value='' style='width:700px;height:50px;font-size:14pt;margin:auto;'><br><input id='btn_query' name='btn_query' type='submit' class='btn btn-default' value='Query' style='width:500px;font-size:14pt;margin:auto;'></form></div>";
        String search = "<form action='/' method='POST'><input class='form-control' id='query_trec' name='query_trec' placeholder='' type='text' value='' style='height:200px;font-size:14pt;text-align:center;'/><input id='btn_query' name='btn_query' type='submit' class='btn btn-default' value='Query' /></form></br>";
        String query;
        query = request.queryParams("query_trec");
        if (!query.contains(":")) {
        query = "contents:" + query;
        }
        SolrDocumentList documents = queryTREC(client, query);
        String keywords = null;
        if (query.contains(":")) {
        keywords = query.split(":")[1];
        }
        if (documents == null) {
        return search_new;
        }

        String results = "";
        for (SolrDocument document : documents) {
        String id = (String) document.getFieldValue("id");
        // String headline = (String) ((ArrayList) document.getFieldValue("headline")).get(0);
        // String profile = (String) document.getFieldValue("profile");
        // String text = (String) ((ArrayList) document.getFieldValue("text")).get(0);
        String contents = (String) ((ArrayList) document.getFieldValue("contents")).get(0);

        Matcher headline_m = headline_tag.matcher(contents);
        Matcher profile_m = profile_tag.matcher(contents);
        Matcher byline_m = byline_tag.matcher(contents);
        Matcher text_m = text_tag.matcher(contents);

        String headline = "";
        String profile = "";
        String byline = "";
        String text = "";

        if (headline_m.find()) {
          headline = headline_m.group(1);
        }
        if (profile_m.find()) {
          profile = profile_m.group(1);
        }
        if (byline_m.find()) {
          byline = byline_m.group(1);
        }
        if (text_m.find()) {
          text = text_m.group(1);
        }

        results += "<b>ID: </b>" + boldWord(id, keywords) + "</br>";
        results += "<b>PROFILE: </b>" + boldWord(profile.replaceAll("\\<.*?>",""), keywords) + "</br>";
        results += "<b>HEADLINE: </b>" + boldWord(headline.replaceAll("\\<.*?>",""), keywords) + "</br>";
        results += "<b>BYLINE: </b>" + boldWord(byline.replaceAll("\\<.*?>",""), keywords) + "</br>";
        results += "<b>TEXT: </b>" + boldWord(text.replaceAll("\\<.*?>",""), keywords) + "</br><hr>";

        }
        return search_new + results;
    });
  }

  private static String boldWord(String str, String toBold) {
    if (str == null || str == "") {
      return "";
    }
    String result = "";
    String[] boldList = toBold.split("\\s+");
    String[] split = str.split("\\s+");
    for (String s : split) {
      for (String bold : boldList) {
        if (s.toUpperCase().contains(bold.toUpperCase())) {
          s = "<mark>" + s + "</mark>";
        }
      }
      result += s + " ";
    }
    return result;
  }

  private static SolrDocumentList queryTREC(SolrClient client, String query) {
    final Map<String, String> queryParamMap = new HashMap<String, String>();
    queryParamMap.put("q", query);
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);
    SolrDocumentList documents = null;
    try {
      Date start = new Date();
      final QueryResponse response = client.query("trec45", queryParams);
      documents = response.getResults();
      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    } catch (Exception e) {
      System.out.println(e.toString());
      return null;
    }
    return documents;
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
