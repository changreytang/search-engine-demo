package com.rtang.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.*;
import org.apache.solr.common.*;


public class TrecSolrDocIterator implements Iterator<SolrInputDocument> {

  protected BufferedReader rdr;
  protected boolean at_eof = false;

  public TrecSolrDocIterator(File file) throws FileNotFoundException {
    rdr = new BufferedReader(new FileReader(file));
    System.out.println("Reading " + file.toString());
  }

  @Override
    public boolean hasNext() {
      return !at_eof;
    }

  @Override
    public SolrInputDocument next() {
      SolrInputDocument doc = new SolrInputDocument();
      StringBuffer sb = new StringBuffer();
      Pattern docno_tag = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
      Pattern headline_tag = Pattern.compile("<HEADLINE>(.+?)</HEADLINE>");
      Pattern profile_tag = Pattern.compile("<PROFILE>(.+?)</PROFILE>");
      Pattern byline_tag = Pattern.compile("<BYLINE>(.+?)</BYLINE>");
      Pattern text_tag = Pattern.compile("<TEXT>(.+?)</TEXT>");
      try {
        String line;

        boolean in_doc = false;
        while (true) {
          line = rdr.readLine();
          if (line == null) {
            at_eof = true;
            break;
          }
          if (!in_doc) {
            if (line.startsWith("<DOC>"))
              in_doc = true;
            else
              continue;
          }
          if (line.startsWith("</DOC>")) {
            in_doc = false;
            sb.append(line);
            break;
          }
          sb.append(line);
        }
        line = sb.toString();

        Matcher docno_m = docno_tag.matcher(line);
        Matcher headline_m = headline_tag.matcher(line);
        Matcher profile_m = profile_tag.matcher(line);
        Matcher byline_m = byline_tag.matcher(line);
        Matcher text_m = text_tag.matcher(line);

        if (docno_m.find()) {
          String docno = docno_m.group(1);
          doc.addField("id", docno);
        }
        if (headline_m.find()) {
          String headline = headline_m.group(1);
          doc.addField("headline", headline);
        }
        if (profile_m.find()) {
          String profile = profile_m.group(1);
          doc.addField("profile", profile);
        }
        if (byline_m.find()) {
          String byline = byline_m.group(1);
          doc.addField("byline", byline);
        }
        if (text_m.find()) {
          String text = text_m.group(1);
          doc.addField("text", text);
        }
        if (sb.length() > 0) {
          doc.addField("contents", sb.toString());
        }
      } catch (IOException e) {
        doc = null;
      }
      return doc;
    }

  @Override
    public void remove() {
      // Do nothing, but don't complain
    }

}
