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

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Client code interface for managing HTTP.
 * Handles sessions, redirects, cookie management, etc.
 * 
 * @author Robin Rawson-Tetley
 * @author Darren Davison
 */
public final class HTTP {
    
    /*
     * This class delegates almost entirely to the package
     * private HTTPHandler
     */
    
    private HTTP() {
        // no instances
    }
    
    /**
     * Returns the header portion for a redirect to a
     * given URL, so your routines can just do:
     * return HTTP.redirect("<url>")
     * @param url The URL to redirect to
     * @return The HTTP response for the redirect
     */
    public static void redirect(String url) {
	    try{
		HTTPHandler h = HTTPHandler.getHandler();
		h.getResponse().sendRedirect( url, false );
	    } catch ( Exception e ){
		    e.printStackTrace();
	    }
	/*
        return  "HTTP/1.1 302 Found\r\n" +
                "Server: OOWeb\r\n" +
                "Location: " + url + "\r\n\r\n";
		*/
    }
    
    
    /**
     * Returns the current session, looked up from the
     * cookie on the current HTTPHandler's thread.  Equivalet
     * of calling <code>getSession(true)</code>
     * @return The session
     */
    public static Map getSession() {
        return getSession(true);
    }

    /**
     * Returns the current session, looked up from the
     * cookie on the current HTTPHandler's thread.
     * @param create true if you wish a new session be created when
     * none exists, false otherwise
     * @return The session or null if no session has
     *         been established yet.
     */
    public static Map getSession(boolean create) {        
        HTTPHandler h = HTTPHandler.getHandler();
        return h.getSession(create);
    }
    
    /**
     * clear the session information and dispose of
     * the session.
     */
    public static void invalidateSession() {    
        HTTPHandler h = HTTPHandler.getHandler();
        h.invalidateSession();
    }
    
    /**
     * Deletes a session 
     * @param sessionId The sessionId used in the session cookie
     */
    public static void deleteSession(String sessionId) {
    	HTTPHandler.deleteSession(sessionId);
    }
    
    /**
     * Returns the cookies supplied with the
     * request as a map.
     * @return A Map of cookies
     */
    public static Map getCookies() {
        HTTPHandler h = HTTPHandler.getHandler();
        return h.getRequestCookies();
    }
    
    /**
     * Allows setting of a temporary cookie
     * @param name The cookie's name
     * @param value The cookie's value
     */
    public static void setCookie(String name, String value) {
        HTTPHandler h = HTTPHandler.getHandler();
        h.getResponse().setCookie(new Cookie(name, value));
    }
    
    /**
     * Allows setting of a permanent cookie or deletion of an existing
     * cookie (by specifying a value for expiry in the past)
     * @param cookie the Cookie to set
     * @see Cookie
     */
    public static void setCookie(Cookie cookie) {
        HTTPHandler h = HTTPHandler.getHandler();
        h.getResponse().setCookie(cookie);
    }

    /**
     * allows a method to determine its own Mime Type.  Defaults
     * to text/html for all methods that do not call this.
     * 
     * @param mimeType the MIME type to set in the output stream
     */
    public static void setMimeType(String mimeType) {
        HTTPHandler h = HTTPHandler.getHandler();
        h.getResponse().setMimeType(mimeType);
    }
    
    /**
     * Allows a method to send a file as the response.
     *
     * Eg:
     * 
     * HTTP.sendFile(new File("myfile.doc"));
     * 
     * @param file The <code>java.io.File</code> to send
     */
    /*
    public static void sendFile(File file) throws IOException {
    	HTTPHandler h = HTTPHandler.getHandler();
    	h.getResponse().sendFile(file, null);
    }
    */
    
    /**
     * Allows a method to send a byte array as the response.
     * 
     * Eg:
     * 
     * HTTP.sendByteStream(new byte[] { 13, 10} , "text/plain");
     * 
     * @param bytes The <code>byte[]</code> to send
     * @param mimeType The mime type to send to the browser
     */
    public static void sendByteArray(byte[] bytes, String mimeType) throws IOException {
    	HTTPHandler h = HTTPHandler.getHandler();
    	h.getResponse().sendByteArray(bytes, mimeType);
    }

}
