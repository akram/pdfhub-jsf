package com.danter.wiki.util;

import javax.servlet.http.HttpServletRequest;

public class ServletUtil {

	public static String createURL(HttpServletRequest request, String resourcePath) {
		 
	    int port = request.getServerPort();
	    StringBuilder result = new StringBuilder();
	    result.append(request.getScheme())
	            .append("://")
	            .append(request.getServerName());
	 
	    if ( (request.getScheme().equals("http") && port != 80) || (request.getScheme().equals("https") && port != 443) ) {
	        result.append(':')
	            .append(port);
	    }
	 
	    result.append(request.getContextPath());
	 
	    if(resourcePath != null && resourcePath.length() > 0) {
	        if( ! resourcePath.startsWith("/")) {
	            result.append("/");
	        }
	        result.append(resourcePath);
	    }
	 
	    return result.toString();
	 
	}

	
}
