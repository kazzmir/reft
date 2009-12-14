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
package net.sf.ooweb.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ooweb.util.Logger;
import net.sf.ooweb.HTTPRequest;
import net.sf.ooweb.util.Base64;



/**
 * SecurityManager.  Because JAAS is pretty horrendous for our simple 
 * needs.
 * <p>
 * This class is a Singleton and acts on behalf of the entire application.
 * 
 * @author Darren Davison
 * @since 0.5
 */
public class SecurityManager {

    public static final String LOGIN_FORM_POST = "/j_security_check";
    
    public static final String USER_SESSION_KEY = "ooweb.user";

    private static final String PROTECTED_KEY = "security.protection.";

    private static final String LOGIN_FORM_KEY = "security.loginForm";
    
    /* singleton */
    private static final SecurityManager instance = new SecurityManager();

    /* specify by client code in ooweb.properties */
    private static final String AUTHENTICATOR_IMPL_KEY = "security.authenticator";
    
    private Properties props = new Properties();
    
    private Map secureMethodCache = new HashMap();

    public static final String LOGIN_REDIRECT_COOKIE_NAME = "ooweb.redirectUrl";

    /**
     * 
     */
    private SecurityManager() {
        // singleton
    }
    
    public static SecurityManager getInstance() {
        return instance;
    }
    
    public void setProperties(Properties p) {
        this.props = p;
    }

    /**
     * If the web application config specifies a login form URL, use it
     * otherwise return null
     * 
     * @return the path of the login form relative to the server root, or
     * null if one is not configured
     */
    public String getLoginUrl() {
        return props.getProperty(LOGIN_FORM_KEY);
    }
    
    /**
     * In similar fashion to the native JDK SecurityManager, ours can
     * veto method calls by throwing appropriate exceptions.  The clients
     * of this class should call this method to verify that the named
     * method is permissable for the current request.
     * 
     * @param request the HTTPRequest under consideration
     * @param session the current session from the request, may NOT be null
     * @throws NotAuthenticatedException if there is no valid credential set
     * available
     * @throws NotAuthorizedException if the authenticated user has not been
     * granted permission to invoke the method on this object
     */
    public String checkRequest(HTTPRequest request, Map session, Logger log) 
    	throws NotAuthenticatedException, NotAuthorizedException {
        
        // first up, are we receiving a login form post?
        String path = request.getPath();
        String redirect = (String) request.getCookies().get(LOGIN_REDIRECT_COOKIE_NAME);
        String role;  // as opposed to spring roll !
        
        if (path.startsWith(LOGIN_FORM_POST)) {
	
	    log.debug("Received authentication post to " + path);
	
            Map postParams = request.getRequestParametersMap();

	    log.debug("Check credentials: user=" + postParams.get("name") + ", pass=" + postParams.get("password"));
	    
            validateCredentials(
                session, (String) postParams.get("name"), (String) postParams.get("password"), log
            );
            
            if (redirect == null) {
                throw new NotAuthenticatedException("Unable to find a redirect cookie in the request!");
	    }
            
            // don't want any query string that may be in the cookie
            int qs = redirect.indexOf("?");
            role = getRoleForPath(qs > 0 ? redirect.substring(0, qs) : redirect);
            
        } else    
            role = getRoleForPath(path);

	log.debug("Checking for role: " + role);
        
        
        // has the method been protected..?
        if (role == null) 
            // ..no - bail out now
            return null;
        
        // ..yes, it has
	log.debug("Found protected role: " + role);

	User user = null;
	if (session != null) user = (User) session.get(USER_SESSION_KEY);
        String authHeader = request.getHeader(HTTPRequest.AUTHORIZATION_HEADER);
        if (user == null && authHeader == null)
            throw new NotAuthenticatedException();
        
        if (user != null) {
            if(!user.hasRole(role)) {
		log.warn("User is not in required role: " + role);
                throw new NotAuthorizedException();
	    }
            else {
                // valid user and user is authorized.  Where next?
                return (path.equals(redirect) ? null : redirect);
	    }
        }
        
        // we must have a value for authHeader to reach here so we'll
        // need to validate the credentials passed
        String credentials = Base64.decode(authHeader.substring(6));
        int sep = credentials.indexOf(":");
        String name = credentials.substring(0, sep);
        String pwd = credentials.substring(sep + 1);
        validateCredentials(session, name, pwd, log);       
        return null;
    }

    private String getRoleForPath(String path) {
        // check method cache first since pattern matching is expensive
        if (secureMethodCache.containsKey(path))
            return (String) secureMethodCache.get(path); // may be null
        
        // need to pattern match the path (absolute) against all of the keys
        // in the props that might be patterns
        for (Iterator i = props.keySet().iterator(); i.hasNext();) {
            String property = (String) i.next();
            if (property.startsWith(PROTECTED_KEY)) {
                String method = property.substring(PROTECTED_KEY.length());
                Pattern p = Pattern.compile(method);
                Matcher m = p.matcher(path);
                if (m.matches()) {
                    // add to cache and return the role for this pattern
                    String role = (String) props.get(PROTECTED_KEY + method);
                    secureMethodCache.put(path, role);
                    return role;
                }
            }
        }
        
        // no match found for this path.  Add to cache as null role
        secureMethodCache.put(path, null);
        return null;
    }

    private void validateCredentials(Map session, String name, String pwd, Logger log) throws NotAuthenticatedException {
        User user;
        try {
	    log.debug("Calling authenticator '" + getAuthenticator().toString() + "' with credentials.");
            user = getAuthenticator().login(name, pwd);
            if (user == null) {
		log.debug("Authentication failed.");
                throw new NotAuthenticatedException();
	    }
            
            synchronized (session) {
                session.put(USER_SESSION_KEY, user);
            }
            
        } catch (Exception e) {
	    log.error("Authentication method threw an exception:");
	    log.error(e);
            throw new NotAuthenticatedException(e.getMessage());
        }
    }

    private Authenticator getAuthenticator() throws NotAuthenticatedException {
        String authClassName = null;
        try {
            authClassName = props.getProperty(AUTHENTICATOR_IMPL_KEY);
            if (authClassName == null)
                throw new NotAuthenticatedException(
                    "In order to protect objects and methods, you need to specify " + 
                    "a class implementing " + Authenticator.class.getName() + 
                    " in your ooweb properties file.");
            
            Class clazz = Class.forName(authClassName);            
            return (Authenticator) clazz.newInstance();
            
        } catch (ClassNotFoundException e) {
            throw new NotAuthenticatedException(
                "You specified a class [" + authClassName + "] implementing " + 
                Authenticator.class.getName() + 
                " in your ooweb properties file, but I couldn't find that class.");
            
        } catch (InstantiationException e) {
            throw new NotAuthenticatedException(
                "You specified a class [" + authClassName + "] implementing " + 
                Authenticator.class.getName() + 
                " in your ooweb properties file, but I couldn't instantiate that class.");
            
        } catch (IllegalAccessException e) {
            throw new NotAuthenticatedException(
                "You specified a class [" + authClassName + "] implementing " + 
                Authenticator.class.getName() + 
                " in your ooweb properties file, but I couldn't access a public, no-arg constructor.");
            
        } catch (ClassCastException e) {
            throw new NotAuthenticatedException(
                "You specified a class [" + authClassName + "].  Does it implement " + 
                Authenticator.class.getName() + 
                " ??");
            
        }
    }

}
