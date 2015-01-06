package com.danter.wiki.util;

import javax.servlet.http.HttpServletRequest;

/*
 * Utility Class for constructing a URL from a resource
 */
public final class ServletUtil
{

   private ServletUtil()
   {
      // seal
   }

   /**
    * constructs a URL from a resource
    */
   public static String createURL(final HttpServletRequest request, final String resourcePath)
   {

      final int port = request.getServerPort();
      final StringBuilder result = new StringBuilder();
      result.append(request.getScheme()).append("://").append(request.getServerName());

      if (("http".equals(request.getScheme()) && port != 80) || ("https".equals(request.getScheme()) && port != 443)) {
         result.append(':').append(port);
      }

      result.append(request.getContextPath());

      if (resourcePath != null && resourcePath.length() > 0) {
         if (!resourcePath.startsWith("/")) {
            result.append('/');
         }
         result.append(resourcePath);
      }

      return result.toString();

   }

}
