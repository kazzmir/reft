/*
 * OOWeb
 *    
 * Copyright(c)2005, OOWeb developers (see the accompanying "AUTHORS" file)
 *
 * This software is licensed under the 
 * GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1
 *    
 * For more information on distributing and using this program, please
 * see the accompanying "COPYING" file.
 */
package net.sf.ooweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.sf.ooweb.util.Logger;
import net.sf.ooweb.util.StringUtils;


/**
 * HTTPRequest handles the parsing of request headers, payloads, cookies and
 * other HTTP request information from the socket's input stream.  It can re-read
 * the stream to fetch more data as required when clients elect to send, for
 * example, POST data in additional packets.
 * 
 * @author Darren Davison
 * @since 0.5
 */
public class HTTPRequest {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    private static final String METHOD_GET = "GET";

    private static final String METHOD_POST = "POST";

    private static final String METHOD_HEAD = "HEAD";
    
    private static final String FORMDATA_URLENCODED = "application/x-www-form-urlencoded";
    
    private static final String FORMDATA_MULTIPART = "multipart/form-data";
    
    private String protocol;
    
    private String httpMethod;
    
    private URL url;
    
    private Map headers = new HashMap();
    
    /** A map of all cookies received in the request */
    private Map cookies = new HashMap();

    private InputStream is;

    private Logger logger;

    private boolean isValid = true;

    private String applicationName;

    private Object[] requestArgs;

    private Map requestArgsMap;
    
    
    /**
     * Construct an HTTPRequest from the application name (used for session cookies), the
     * InputStream that the raw request data should be read from, and a logger.
     * <p>
     * Automatically reads data from the stream and parses request headers and cookies
     * from it.  
     * <p>
     * This constructor is package private - it makes no sense to instantiate one
     * outside of the package, but some of its data is universally useful
     * 
     * @param applicationName
     * @param is
     * @param logger
     * @throws IOException if data cannot be read from the stream for any reason
     */
    HTTPRequest(String applicationName, InputStream is, Logger logger) throws IOException {
        super();
        this.is = is;
        this.logger = logger;
        this.applicationName = applicationName;
        
        // Read the request into our buffer, or throw the resulting exception
        String req = readHeadersFromStream();
        logger.debug("Received request:\n" + req);

        try {
            parseRequestHeaders(req);
            logger.debug("Handling: " + httpMethod + " " + 
                url.toString());
            logger.debug("URL is [" + url.getPath() + 
                "] and querystring is [" + url.getQuery() + "]");
        } catch (Exception e) {
            isValid  = false;
        }
        
        parseCookies((String) headers.get("Cookie"));
        
    }

    /*
     * Reads the HTTP headers from the stream - stops once
     * a double line break (\r\n\r\n) is reached or we run
     * out of buffer.
     */
    private String readHeadersFromStream() throws IOException {        
        
    	StringBuffer b = new StringBuffer();
    	int nb = is.read();
    	
    	if (nb == -1)
    		throw new IOException("Failed to read any data from the request stream");
    	
    	// Track the last 4 bytes read. I know, I could have used
    	// the last 4 chars in the string buffer, but slicing 
    	// strings is inefficient by comparison
    	int[] l4b = new int[4];
    	int l4i = 0;
    	l4b[0] = 0; l4b[1] = 0; l4b[2] = 0; l4b[3] = 0;
    	
    	while (nb != -1) {
    		b.append((char) nb);
    		nb = is.read();
    		
    		l4b[l4i] = nb;
    		l4i++; if (l4i == 4) l4i = 0;
    		
    		// Stop reading if the last 4 chars were breaks -
    		// we have our headers.
    		if ( (l4b[0] == 10 || l4b[0] == 13) && 
    			 (l4b[1] == 10 || l4b[1] == 13) &&
    			 (l4b[2] == 10 || l4b[2] == 13) &&
    			 (l4b[3] == 10 || l4b[3] == 13) )
    			break;
    	}

        return b.toString();
    }
    
    /**
     * Reads the payload of a POST from the stream (assumes the
     * stream is at the correct point through use of
     * the <code>readHeadersFromStream</code> method.
     * @return The payload as a byte array
     * @throws IOException
     */
    private byte[] readPayloadFromStream() throws IOException {
    	
    	byte[] b = new byte[4096];
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	
    	int read = is.read(b);
    	if (read == -1)
    		throw new IOException("Failed to read any data from the request stream");
    	os.write(b, 0, read);
    	
    	// Loop through the stream and dump it to the 
    	// bytearray
    	try {
	    	if (is.available() > 0) {
		    	while (read != -1) {
		    		read = is.read(b);
		    		if (read != -1) { 
		    			os.write(b, 0, read);
		    		}
		    	}
	    	}
    	}
    	catch (SocketTimeoutException e) {
    		// Don't do anything - we've read all the data
    	}
    	
    	return os.toByteArray();
    }

    /*
     * generate the Map of request header name/value pairs from the raw request String
     */
    private void parseRequestHeaders(String request) throws MalformedURLException {
        // Break up the lines of the request 
        int i = request.indexOf("\r");
        int l = 0;
        String n = "";
        while (i != -1) {
            // l -> i = next item
            n = request.substring(l, i);
            // If it's a \r\n, throw the \n away
            if (n.startsWith("\n")) n = n.substring(1, n.length());
            if (n.endsWith("\n")) n = n.substring(0, n.length()-1);
            if (l == 0) {
                // get protocol, method, URL
                String[] helloLine = n.split(" ");
                httpMethod = helloLine[0];
                url = new URL("http://host" + helloLine[1]);
                protocol = helloLine[2];
                logger.debug(
                    "httpMethod [" + httpMethod + 
                    "] received for URL [" + url.getPath() + 
                    "] with protocol [" + protocol + "]");
                                
            } else if (n.indexOf(":") > -1) {
                int j = n.indexOf(":");
                headers.put(n.substring(0, j), n.substring(j + 2));
                
            } 
            
            l = i+1;
            i = request.indexOf("\r", l);
        }        
        
    }
    
    /* 
     * Searches for the Cookie: line in the 
     *  request and parses the cookies into the 
     *  cookies map.
     */
    private void parseCookies(String cookieHeader) {
        if (cookieHeader != null) {
            logger.debug("Parsing cookes from request header [" + cookieHeader + "]");            
            String[] c = StringUtils.split(cookieHeader, ";");
            logger.debug("Got " + c.length + " cookies.");
            for (int z = 0; z < c.length; z++) {
                String cookie = c[z].trim();
                int eqls = cookie.indexOf('=');
                String n = cookie.substring(0, eqls);
                String v = cookie.substring(eqls + 1);
                logger.debug("Adding cookie '" + n + "' with value '" + v + "'");
                cookies.put(n, v);
            }
            
        } else
            logger.debug("No cookies found in request headers");
    }

    /**
     * Returns the Cookie: line from the HTTP request
     * that contains the session cookie. If one
     * doesn't exist, null is returned.
     * @return The session cookie or null
     */
    String getSessionCookie() {
        Object c = cookies.get(applicationName);
        if (c == null) return null;
        return c.toString();
    }
    
    /**
     * Returns the path component of the underlying URL used to make the
     * request.  Delegates to <code>java.net.URL.getPath()</code>
     * 
     * @return the path of the request which may include a query string
     */
    public String getPath() {
        return url.getPath();
    }

    /**
     * Return the value of a named request header, or null if no such
     * header was found
     * 
     * @param header the name of the header such as "If-Modified-Since"
     * @return the value of the header if found in the request, or null
     */
    public String getHeader(String header) {
        return (String) headers.get(header);
    }
    
    /**
     * Return the HTTP method used in this request, for example 'GET'
     * or 'POST'
     * 
     * @return the HTTP method used in the request
     */
    public String getHttpMethod() {
        return httpMethod;
    }
    
    /**
     * Return the protocol from the request, for example 'HTTP/1.1'
     * 
     * @return the HTTP protocol and version
     */
    public String getProtocol() {
        return protocol;
    }
    
    /**
     * Return a Map of the cookies found in this request.  The Map
     * may be empty if no cookies were sent by the client.
     * 
     * @return a Map of cookies in name/value form
     */
    public Map getCookies() {
        return cookies;
    }
    
    /**
     * Determines whether the request looks valid from an RFC point of view.
     * Not fully implemented, don't rely on it.
     * 
     * @return true if the request appears valid, false otherwise
     */
    public boolean isValid() {
        return isValid;
    }
    
    /**
     * convenience method to determine if the request was an HTTP
     * POST
     * 
     * @return true if the request was an HTTP POST, false otherwise
     */
    public boolean isPost() {
        return httpMethod.equals(METHOD_POST);
    }
    
    /**
     * convenience method to determine if the request was an HTTP
     * GET
     * 
     * @return true if the request was an HTTP GET, false otherwise
     */
    public boolean isGet() {
        return httpMethod.equals(METHOD_GET);
    }

    public boolean isHead(){
	    return httpMethod.equals(METHOD_HEAD);
    }

    /**
     * Is the request an HTTP 1.1 request or not?
     * 
     * @return true if client sent a 1.1 method request
     */
    public boolean isHttp11() {
        return protocol.indexOf("1.1") > -1;
    }
    
    /**
     * Get an array of request params.  Each item in the array is either:
     * 1: A String in the form "name=value".
     * 2: A FormEncodedFile object sent by the browser
     * 
     * @return the array of request parameters, or null if none were
     * found in the request
     */
    public Object[] getRequestParameters() {
        
    	// We can have multiple form parts, so we aggregate
    	// them all together where necessary.
        Object[] data = getParamsFromGetOrPost();
        if (data == null) return null;
        
        requestArgs = null;
        Vector args = new Vector();
        
        for (int i = 0; i < data.length; i++) {
	        
	        // If the object we got back is a String, 
	        // it's either a querystring or URL encoded form data -
	        // either way, we parse it the same
	        if (data[i] instanceof String) {
	        	String querystring = data[i].toString(); 
	            if (querystring != null) {
	                logger.debug("Querystring/URLencoded data found, splitting into separate arguments");
	                String[] bits = StringUtils.split(querystring, "&");
	                for (int z = 0; z < bits.length; z++) {
	                    logger.debug("Found argument " + z + ": " + bits[z]);
	                    // Undo encoded strings
	                    args.add( StringUtils.decodeURLEncoding(bits[z]) );
	                    logger.debug("URLDecoded argument " + z + " to '" + bits[z] + "'");
	                }
	            }
	        }
	        // The user posted us a file
	        else if (data[i] instanceof FormEncodedFile) {
	        	args.add(data[i]);
	        }
        }
        
        requestArgs = args.toArray();
        return requestArgs;
    }
    
    /**
     * Get a Map of request parameters with key/value pairs for the request
     * parameters
     * 
     * @return the Map of request parameters and values, or null if none 
     * were found in the request
     */
    public Map getRequestParametersMap() {
        
    	if (requestArgsMap != null) 
            return requestArgsMap;
        
        if (requestArgs == null)
            getRequestParameters();
        
        // messy.  TODO: fix
        if (requestArgs == null)
            return null;
        
        Map map = new HashMap(requestArgs.length);
        for (int i = 0; i < requestArgs.length; i++) {
        	if (requestArgs[i] instanceof String) {
	            int eq = requestArgs[i].toString().indexOf("=");
	            if (eq != -1) {
	                String k = requestArgs[i].toString().substring(0, eq);
	                String v = requestArgs[i].toString().substring(eq + 1);
	                logger.debug("Adding map var key=" + k + " value=" + v);
	                map.put(k, v);
	            }
        	}
        	else if (requestArgs[i] instanceof FormEncodedFile) {
        		String k = ((FormEncodedFile) requestArgs[i]).getName();
        		logger.debug("Adding map var for file key=" + k + " value=" + requestArgs[i].toString());
        		map.put(k, requestArgs[i]);
        	}
        }
        
        // cache the map
        requestArgsMap = map;
        return map;
    }

    URL getUrl() {
        return url;
    }
    
    /*
     * returns the raw query string(s) whether supplied on a GET or POST
     */
    private Object[] getParamsFromGetOrPost() {        
        String qs = url.getQuery(); 
        if (qs != null) return new Object[] {qs};
        
        if (isPost()) 
            return getPostParameters();
        else
            return null;
    }

    /**
     * 
     * @return
     */
    ObjectAndMethod getObjectAndMethod() {
        String method = "index";
        String object = "";
        String urlPath = url.getPath();
        
        if (!urlPath.endsWith("/")) {
            int lastSplit = urlPath.lastIndexOf("/");
            method = urlPath.substring(lastSplit + 1);
            object = urlPath.substring(0, lastSplit + 1);
            logger.debug("URLbase does not end with /, split into method [" + method + "] on path [" + object + "]");
        }
        else {
            object = urlPath;
            logger.debug("URLbase ends with a /, method defaults to [index]");
        }
        return new ObjectAndMethod(method, object);        
    }

    /*
     * Gets POST params by using the 
     * <code>readPayloadFromStream()</code>
     * method. Returns an object array containing either
     * Strings of URL encoded data, or FormEncodedFile
     * objects.
     */
    private Object[] getPostParameters() {
        
    	byte[] postData = null;

        try {
            postData = readPayloadFromStream();
        } catch (IOException e) {
            logger.error(e);
            return null;
        }            
        
        logger.debug("Read payload data.");
        
        // If we had a content type in the request params already,
        // and it's a URL encoded one, the payload is the form
        // and we don't need to parse anything else
        String hct = (String) headers.get("Content-Type");
        if (hct == null) hct = (String) headers.get("Content-type");
        if (hct != null && hct.toLowerCase().trim().equals(FORMDATA_URLENCODED)) {
        	logger.debug("Headers specified URL encoded content type, returning payload as form.");
        	return new Object[] { new String(postData) };
        }
        
        // If it's multipart formdata, locate all parts
        // and parse them.
        if (hct != null && hct.toLowerCase().trim().startsWith(FORMDATA_MULTIPART)) {
        	logger.debug("Headers specified multipart form data, parsing.");
        	
        	// Read the boundary info from the Content-Type Header
        	int bpos = hct.indexOf("=");
        	if (bpos == -1) {
        		logger.error("Couldn't find Boundary marker in formdata content type!");
        		return null;
        	}
        	String boundary = hct.substring(bpos + 1);
        	logger.debug("Found formdata boundary marker: " + boundary);
        	
        	// Holds the different form parts found
        	Vector parts = new Vector();
        	
        	// Loop through the entire form data and break it
        	// into chunks based on the boundaries, then process
        	// them.
        	String dbuff = new String(postData);
        	int bsp = dbuff.indexOf(boundary);
        	
        	while (bsp != -1) {
	        	int bep = dbuff.indexOf(boundary, bsp + boundary.length() + 2);
	        	if (bep == -1) break;
	        	String head = dbuff.substring(bsp, bep);
	        	
	        	// If the content type for this part has no filename,
	        	// then it's the data from a form component, get it's
	        	// name and construct a URL encoded string to send back
	        	if (head.indexOf("filename=") == -1) {
	        		// Get its name
	        		String name = "";
	        		int sp = head.indexOf("name=");
	        		if (sp != -1) {
	        			int ep = head.indexOf(";", sp);
	        			if (ep == -1) ep = head.indexOf("\r", sp);
	        			name = head.substring(sp + "name=".length(), ep);
	        			if (name.startsWith("\"")) name = name.substring(1);
	        			if (name.endsWith("\"")) name = name.substring(0, name.length() -1);
	        		}
	        		String urldata = head.substring(head.indexOf("\r\n\r\n"));
	        		urldata = urldata.trim();
	        		urldata = StringUtils.encodeURL(urldata);
	        		String thedata = name + "=" + urldata;
	        		parts.addElement(thedata);
	        		logger.debug("Got URL encoded data: " + thedata);
	        		urldata = null;
	        		name = null;
	        		thedata = null;
        		}
	        	else {
	        		// Otherwise, it must be a file
	        		parts.add(parseEncodedFile( postData, bsp + boundary.length(), bep));
	        	}
	        	// Set to next marker
	        	bsp = bep + boundary.length();
        	}
        	
        	// Return all form parts
        	return parts.toArray();
        	
        }
        
        if (hct == null) {
        	logger.error("No content type was specified in the request. I don't know what to do!");
        }
        return null;
        
    }
    
    /**
     * Locates the first double break (\r\n\r\n) in a byte
     * array.
     * @param data
     * @param startAt
     * @return The location or -1 if none was found
     */
    private int findDoubleBreak(byte[] data, int startAt) {
    	int out = -1;
        for (int i = startAt; i < data.length-4; i++) {
        	if (
        		(data[i] == 13 || data[i] == 10) && 
        		(data[i+1] == 13 || data[i+1] == 10) &&
        		(data[i+2] == 13 || data[i+2] == 10) &&
        		(data[i+3] == 13 || data[i+3] == 10) 
        		) {
        		out = i+4;
        		break;
        	}
        }
        return out;
    }
    
    /**
     * Given a lump of data representing a file (including headers),
     * this function will parse the header info/data into a 
     * <code>FormEncodedFile</code> object.
     * 
     * @param data The data as an array of bytes
     * @param start The position in the data where the file
     *              starts (first byte after the $boundary)
     * @param end The position in the data where the file ends
     *            (last byte before the $boundary)
     * @return A parsed FormEncodedFile object
     */
    private FormEncodedFile parseEncodedFile(byte[] data, int start, int end) {
    	
    	int headerend = findDoubleBreak(data, start);
    	
    	String name = "";
    	String filename = "";
    	String mimeType = "";
    	byte[] filedata = null;
    	
    	if (headerend == -1) {
    		logger.debug("Couldn't find headers for file - assuming everything under boundary is file.");
    		headerend = start;
    	}
    	else {
    		String headers = new String(data, start, headerend - start);
    		logger.debug("Found form encoded file headers: " + headers);
    		
    		// name
    		int sp = headers.indexOf("name=");
    		if (sp != -1) {
    			int ep = headers.indexOf(";", sp);
    			if (ep == -1) ep = headers.indexOf("\r", sp);
    			name = headers.substring(sp + "name=".length(), ep);
    			if (name.startsWith("\"")) name = name.substring(1);
    			if (name.endsWith("\"")) name = name.substring(0, name.length() -1);
    			logger.debug("Found form name for file: " + name);
    		}
    		
    		// filename
    		sp = headers.indexOf("filename=");
    		if (sp != -1) {
    			int ep = headers.indexOf(";", sp);
    			if (ep == -1) ep = headers.indexOf("\r", sp);
    			filename = headers.substring(sp + "filename=".length(), ep);
    			if (filename.startsWith("\"")) filename = filename.substring(1);
    			if (filename.endsWith("\"")) filename = filename.substring(0, filename.length() -1);
    			logger.debug("Found file name for file: " + filename);
    		}
    		
    		// Mimetype
    		sp = headers.indexOf("Content-Type: ");
    		if (sp != -1) {
    			sp += "Content-Type: ".length();
    			mimeType = headers.substring(sp, headers.indexOf("\r", sp));
    			logger.debug("Found mimetype for file: " + mimeType);
    		}
    	}
    	
    	// Can't give it a blank name, use the filename if available
    	if (name.equals("")) {
    		name = filename;
    	}
    	// If there's no filename, use the hashcode of the string as
    	// something
    	if (name.equals("")) {
    		name = Integer.toString(name.hashCode());
    		logger.debug("File item has no name, using arbitrary hashcode: " + name);
    	}
    	
    	// Copy the file data into a new array for storage
    	int datalen = end - headerend;
    	filedata = new byte[datalen];
    	System.arraycopy(data, headerend, filedata, 0, datalen);
    	
    	FormEncodedFile f = new FormEncodedFile(name, filename, mimeType, filedata);
    	return f;
    }

}
