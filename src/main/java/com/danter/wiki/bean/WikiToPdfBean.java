package com.danter.wiki.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.danter.wiki.util.ConvertGithubWiki;
import com.danter.wiki.util.ServletUtil;
import com.lowagie.text.DocumentException;

/**
 * 
 * @author mdanter
 */
@Named
@ViewScoped
public class WikiToPdfBean implements Serializable
{

   private static final long serialVersionUID = 6604038069342036160L;
   private static final Logger LOG = LoggerFactory
            .getLogger(WikiToPdfBean.class);

   private String contextPath;

   private String wikiUrl;
   private DualListModel<String> wikiPages;
   private Boolean includeHeader = Boolean.TRUE;
   private Boolean includePageNums = Boolean.TRUE;
   private Boolean includeGithubStyles = Boolean.FALSE;
   private String wikiTitle;

   private StreamedContent pdf;

   /**
    * Creates a new instance of WikiToPdfBean
    */
   public WikiToPdfBean()
   {

      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
               .getRequest();

      contextPath = ServletUtil.createURL(request, "");
   }

   public void goBtnAction()
   {

      if (wikiUrl == null || wikiUrl.isEmpty()) {
         wikiUrl = "https://github.com/redhat-consulting/jbpm-ee/wiki";
      }
      try {
         ConvertGithubWiki converter = new ConvertGithubWiki(wikiUrl, contextPath);

         List<String> pagesTarget = new ArrayList<String>();

         wikiPages = new DualListModel<String>(converter.getPageLinkNames(),
                  pagesTarget);

         wikiTitle = converter.getTitle();

      }
      catch (Exception ex) {
         LOG.warn(ex.getMessage());
         FacesContext
                  .getCurrentInstance()
                  .addMessage(
                           null,
                           new FacesMessage(FacesMessage.SEVERITY_ERROR, "wrong url", wikiUrl
                                    + " does not start with http and end with wiki"));

      }
   }

   public String clearAction()
   {
      wikiUrl = null;
      wikiPages = null;
      wikiTitle = null;
      return "?faces-redirect=true";
   }

   public String getWikiUrl()
   {
      return wikiUrl;
   }

   public void setWikiUrl(String wikiUrl)
   {

      this.wikiUrl = wikiUrl.trim();
   }

   public DualListModel<String> getWikiPages()
   {
      return wikiPages;
   }

   public void setWikiPages(DualListModel<String> wikiPages)
   {
      this.wikiPages = wikiPages;
   }

   public Boolean getIncludeHeader()
   {
      return includeHeader;
   }

   public void setIncludeHeader(Boolean includeHeader)
   {
      this.includeHeader = includeHeader;
   }

   public String getWikiTitle()
   {
      return wikiTitle;
   }

   public void setWikiTitle(String wikiTitle)
   {
      this.wikiTitle = wikiTitle;
   }

   public Boolean getIncludePageNums()
   {
      return includePageNums;
   }

   public void setIncludePageNums(Boolean includePageNums)
   {
      this.includePageNums = includePageNums;
   }

   public Boolean getIncludeGithubStyles()
   {
      return includeGithubStyles;
   }

   public void setIncludeGithubStyles(Boolean includeGithubStyles)
   {
      this.includeGithubStyles = includeGithubStyles;
   }

   public StreamedContent getPdf() throws IOException
   {

      pdf = null;

      if (receivedGoodInput()) {

         String fileName = wikiUrl.replaceAll("http(s)*://github.com/", "")
                  .replaceAll("/", "_").replace("_wiki", "")
                  + new SimpleDateFormat("-yyyy-M-dd_HH-mm-ss-SS").format(new Date()) + ".pdf";

         ConvertGithubWiki converter = new ConvertGithubWiki(wikiUrl, contextPath);
         String htmlDocument = null;
         try {
            htmlDocument = converter
                     .constructDocument(wikiTitle, wikiPages.getTarget(), includeHeader, includePageNums);
         }
         catch (URISyntaxException e) {
            LOG.error(e.getMessage());
         }

         ByteArrayOutputStream os = null;
         try {
            os = new ByteArrayOutputStream();

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlDocument);
            renderer.layout();

            renderer.createPDF(os);

            ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
            pdf = new DefaultStreamedContent(bais, "application/pdf", fileName);

         }
         catch (DocumentException ex) {

            LOG.error(ex.getMessage());

         }
         finally {
            try {
               os.flush();
               os.close();

            }
            catch (IOException ex) {
               LOG.error(ex.getMessage());
            }

         }
      }

      return pdf;
   }

   /**
    * Validation for page components.
    */
   private Boolean receivedGoodInput()
   {

      boolean goodInput = true;

      if (wikiPages.getTarget().isEmpty()) {

         FacesContext.getCurrentInstance().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_WARN, "empty document", "select the pages first"));
         goodInput = false;

      }
      else if (!wikiUrl.endsWith("wiki") && !wikiUrl.endsWith("wiki/") && !wikiUrl.startsWith("http")) {

         goodInput = false;
         FacesContext
                  .getCurrentInstance()
                  .addMessage(
                           null,
                           new FacesMessage(FacesMessage.SEVERITY_ERROR, "wrong url", wikiUrl
                                    + " does not start with http and end with wiki"));

      }

      return goodInput;

   }

}
