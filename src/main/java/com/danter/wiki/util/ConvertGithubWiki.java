package com.danter.wiki.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;


/**
 * @author mdanter
 * 
 */
public class ConvertGithubWiki
{

   private static final Logger LOG = LoggerFactory.getLogger(ConvertGithubWiki.class);

   private String contextPath;
   private String wikiUrl;
   private Elements wikiPageLinks;

   public ConvertGithubWiki(final String wikiUrl, final String contextPath)
   {
      this.wikiUrl = wikiUrl;
      this.contextPath = contextPath;
   }

   public String constructDocument(String title, List<String> selectedPageNames, Boolean includeHeader,
            Boolean includePageNums) throws IOException, URISyntaxException
   {

      
      
      wikiPageLinks = getPageLinks();

      final StringBuilder result = new StringBuilder();
      result.append(getHeader(includeHeader, includePageNums));
      result.append("<h1>");
      result.append(title);
      result.append("</h1>");
      result.append(getBody(wikiPageLinks, selectedPageNames));
      result.append(getFooter());

      Document doc = Jsoup.parse(result.toString());
      doc.outputSettings().syntax(Syntax.xml);

      return doc.toString();

   }

   public String constructRestDocument(String title, List<String> selectedUrls, Boolean includeHeader,
            Boolean includePageNums) throws IOException, URISyntaxException
   {

      final StringBuilder result = new StringBuilder();
      result.append(getHeader(includeHeader, includePageNums));
      result.append("<h1>");
      result.append(title);
      result.append("</h1>");
      result.append(getBody(selectedUrls));
      result.append(getFooter());

      Document doc = Jsoup.parse(result.toString());
      doc.outputSettings().syntax(Syntax.xml);

      return doc.toString();

   }

   public List<String> getPageLinkNames() throws IOException
   {

      Elements pageLinks = getPageLinks();
      List<String> linkNames = new ArrayList<String>();

      for (Element pageLink : pageLinks) {

         String pageTitle = pageLink.text();
         linkNames.add(pageTitle);
      }
      return linkNames;
   }

   public List<String> getPageLinkHrefs() throws IOException
   {

      Elements pageLinks = getPageLinks();
      List<String> linkNames = new ArrayList<String>();

      for (Element pageLink : pageLinks) {
         String href = pageLink.attr("abs:href");
         linkNames.add(href);
      }
      return linkNames;
   }

   public String getTitle() throws IOException
   {
      Document doc = Jsoup.connect(wikiUrl + "/_pages").get();
      String docTitle = getMetaTag(doc, "description");
      return docTitle;
   }

   // public helper method to save PDF output
   public static void savePdf(String htmlDocument, String outputFile)
   {

      OutputStream os = null;
      try {
         os = new FileOutputStream(outputFile);

         ITextRenderer renderer = new ITextRenderer();
         renderer.setDocumentFromString(htmlDocument);
         renderer.layout();

         renderer.createPDF(os);
      }
      catch (Exception e) {
         LOG.error(e.getMessage());
      }
      finally {
         try {
            os.flush();
            os.close();
         }
         catch (IOException e) {
            LOG.error(e.getMessage());
         }
      }

   }

   // public helper method to save HTML output
   public static void saveHtml(String fullDocument, String outputFile)
            throws FileNotFoundException
   {
      PrintWriter out = new PrintWriter(outputFile);
      out.write(fullDocument);
      out.flush();
      out.close();
   }

   private Elements getPageLinks() throws IOException
   {
      Document doc = Jsoup.connect(wikiUrl + "/_pages").get();
      Element body = doc.select("div.file-wrap").first();

      Elements pageLinks = body.select("a[href]");
      return pageLinks;
   }

   private String getHeader(boolean includeHeader, Boolean includePageNums)
   {
      final StringBuilder result = new StringBuilder();
      result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      result.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
      result.append("<html>");
      result.append("<head>");
      result.append("<meta charset='utf-8' />");
      result.append("<style type=\"text/css\">");
      result.append("img{width:100% !important;height:auto !important;}");
      result.append("code{white-space: pre;}");
      result.append("pre {white-space: pre-wrap !important;white-space: -moz-pre-wrap !important;white-space: -pre-wrap !important;white-space: -o-pre-wrap !important;word-wrap: break-word; !important}");

      if (includeHeader) {
         result.append("@page {margin-top: 2.5cm;background:#ffffff url('");
         result.append(contextPath);
         result.append("/resources/images/redhat_logo.jpg') no-repeat 16px 16px;background-size: auto 60px;}");
      }
      if (includePageNums) {
         result.append("#footer {position: running(footer);text-align: right;}");
         result.append("@page {@bottom-right {content: element(footer);}}");
         result.append("#pagenumber:before {content: counter(page);}");
         result.append("#pagecount:before {content: counter(pages);}");
      }

      result.append("body {font-family: Sans-Serif;}</style>");

      result.append("</head><body>");

      if (includePageNums) {
         result.append("<div id=\"footer\">page <span id=\"pagenumber\"></span> of <span id=\"pagecount\"></span></div>");
      }
      return result.toString();
   }

   private static String getBody(Elements pages, List<String> selectedPageNames)
            throws IOException, URISyntaxException
   {

      final StringBuilder result = new StringBuilder();
      

      for (String selectedName : selectedPageNames) {
         for (Element pageLink : pages) {

            String href = pageLink.attr("abs:href");
            String pageTitle = pageLink.text();

            if (selectedName.equals(pageTitle)) {

               Document pageDoc = Jsoup.connect(href).get();
               Element wikiContent = pageDoc.select("div.markdown-body").first();
               wikiContent.select(":containsOwn(\u00a0)").remove();

               String htmlText = "<h2>" + pageTitle + "</h2>" + wikiContent;
               result.append(cleanHtmlOutput(htmlText, href));
            }
         }
      }

      return result.toString();
   }

   private static String getBody(List<String> pageUrls) throws IOException,
            URISyntaxException
   {

      final StringBuilder result = new StringBuilder();

      for (final String url : pageUrls) {

         Document pageDoc = Jsoup.connect(url).get();
         Element wikiContent = pageDoc.select("body").first();

         result.append(cleanHtmlOutput(wikiContent.toString(), url));
      }

      return result.toString();
   }

   private static String cleanHtmlOutput(final String htmlText,
            final String href)
   {

      String output;
      Document pageDoc = Jsoup.parse(htmlText);
      pageDoc.outputSettings().escapeMode(EscapeMode.xhtml).charset("UTF-8");

      output = Jsoup.clean(pageDoc.toString(), href, Whitelist.relaxed()
               .preserveRelativeLinks(false));
      return output;
   }

   private static String getFooter()
   {
      return "</body></html>";
   }

   private static String getMetaTag(final Document document, final String attr)
   {
      Elements elements = document.select("meta[name=" + attr + "]");
      for (Element element : elements) {
         final String s = element.attr("content");
         if (s != null) {
            return s;
         }
      }
      elements = document.select("meta[property=" + attr + "]");
      for (Element element : elements) {
         final String s = element.attr("content");
         if (s != null) {
            return s;
         }
      }
      return null;
   }

}
