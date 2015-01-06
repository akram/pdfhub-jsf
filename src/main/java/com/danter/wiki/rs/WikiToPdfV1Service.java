package com.danter.wiki.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.danter.wiki.entity.PdfJsonObject;
import com.danter.wiki.util.ConvertGithubWiki;
import com.danter.wiki.util.ServletUtil;

@Path("/v1")
public class WikiToPdfV1Service
{

   private static final Logger LOG = LoggerFactory
            .getLogger(WikiToPdfV1Service.class);

   @Context
   private HttpServletRequest servletRequest;

   @POST
   @Produces("application/pdf")
   @Path("/pdf")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response pdfFromList(PdfJsonObject json) throws IOException,
            URISyntaxException
   {

      LOG.warn(json.toString());

      ConvertGithubWiki converter = new ConvertGithubWiki("", ServletUtil.createURL(servletRequest, ""));

      String htmlDocument = converter.constructRestDocument(json.title,
               json.urls, Boolean.TRUE, Boolean.TRUE);

      final ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(htmlDocument);
      renderer.layout();

      StreamingOutput stream = new StreamingOutput() {
         public void write(OutputStream output) throws IOException,
                  WebApplicationException
         {
            try {
               renderer.createPDF(output);
            }
            catch (Exception e) {
               throw new WebApplicationException(e);
            }
         }
      };

      return Response
               .ok(stream)
               .header("content-disposition",
                        "attachment; filename = export.pdf").build();

   }
}
