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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.ooweb.security.NotAuthenticatedException;
import net.sf.ooweb.security.NotAuthorizedException;
import net.sf.ooweb.security.SecurityManager;
import net.sf.ooweb.sessions.Session;
import net.sf.ooweb.sessions.SessionFactory;
import net.sf.ooweb.util.Logger;

import com.rafkind.reft.FileTreeNode;

/**
 * Handles HTTP messages from the request, delegating to the response to manage
 * the output streams.
 * 
 * @author Robin Rawson-Tetley
 * @author Darren Davison
 */
class HTTPHandler{

	private static final String USER_ERROR_METHOD = "handleError";

	/** A map of all currently running HTTPHandlers, indexed
	 *  by their thread hashcodes. */
	private static Map runningHandlers = new HashMap();

	/**
	 * Map of session objects (ooweb.sessions.Session descendants)
	 */
	private static HashMap sessions = new HashMap();

	/** The OOWeb server object */
	private Server server = null;

	/** Logger to use for logging (!) */
	private Logger logger;

	private HTTPRequest request;

	private HTTPResponse response;

	private SecurityManager securityManager = SecurityManager.getInstance();

	/**
	 * Retrieves the handler for the current thread
	 */
	static HTTPHandler getHandler() {
		synchronized (runningHandlers) {
			return (HTTPHandler) runningHandlers.get( Thread.currentThread().getName() );
		}
	}

	/**
	 * @param srv The server object
	 * @param ps The output stream to write the response on
	 * @param request The incoming request.
	 * @param logger the logger to use
	 */
	HTTPHandler(Server srv, HTTPRequest request, HTTPResponse response, Logger logger) {
		this.server = srv;
		this.logger = logger;
		this.request = request;
		this.response = response;
		initHandler();
	}

	private void initHandler() {
		if (request.getHttpMethod().equals(server.getShutDownPassword())) {
			server.shutdown();

		} else {

			try {
				if (!request.isValid()) {
					handleHttpException(
							new HTTPException(
								HTTPResponse.CODE_500, "Request is not a valid"));
					return;            
				}

				addHandler();                
				response.setSessionCookieRequired(request.getSessionCookie() == null);

				// supported method?
				if (request.isGet() || request.isPost() || request.isHead() )
					handleGetPost();
				else
					handleHttpException(
							new HTTPException(
								HTTPResponse.CODE_400, 
								request.getHttpMethod() + " is not a supported method"));

			} catch (HTTPException e) {
				// protocol error - we can handle this
				logger.error( e );
				handleHttpException(e);

			} catch (ResponseCommittedException e) {
				// oops.  This is pretty much fatal, we can't deal with it
				logger.error(e);
				logger.error("This is likely to be a programming error");
			}
		}

		// Clear anything up
		dispose();
	}

	private void handleHttpException(HTTPException e) {
		// is there a user defined error page?
		Method userErrorHandlerMethod;
		try {
			Object[] params = new Object[] {
				e.getHttpCode(), e.getMessage()
			};

			Object rootObject = server.getRegisteredObject("/");
			userErrorHandlerMethod =
				getMethodToInvoke(params, USER_ERROR_METHOD, rootObject);
			logger.debug("Calling user error handler at [" + USER_ERROR_METHOD + "]");

			String errorPage = (String) 
				userErrorHandlerMethod.invoke(rootObject, params);
			response.sendError(e, errorPage);

		} catch (Exception caught) {
			// let's avoid an infinite loop eh??
			logger.debug(
					"User code does not define an error handling method, or it wasn't possible to call the method");
			response.sendError(e, null);
		}
	}

	/**
	 * @return the map of cookies in the request
	 */
	Map getRequestCookies() {
		return request.getCookies();
	}

        Session getSessionObject(boolean create){
            String id = request.getSessionCookie();
            Session s = (Session) sessions.get(id);

            // If no session was found, create one
            // and return that instead
            if (s == null && create) {          
                s = SessionFactory.createSession();
                s.create(id);
                synchronized (sessions) {
                    sessions.put(id, s);
                }
            }

            // return null if no session exists now
            if (s == null)
                return null;

            // Accessed, so "touch" the session to prevent
            // expiry for another timeout period.
            s.touch();
            return s;
        }

	/**
	 * Get the current Session
	 * 
	 * @param create determines whether to create a new session
	 * if a prior one wasn't found
	 * @return the session for this request, based on the session cookie
	 * in the request headers
	 */
	Map getSession(boolean create) {
            Session s = getSessionObject(create);
            if (s != null){
                return s.getMap();
            }
            return null;
	}

	void invalidateSession() {
		String id = request.getSessionCookie();
		synchronized (sessions) {
			((Session) sessions.get(id)).dispose();
			sessions.remove(id);
		}
	}

	public static void deleteSession(String id) {
		synchronized (sessions) {
			sessions.remove(id);
		}
	}

	HTTPResponse getResponse() {
		return response;
	}

	/**
	 * Adds this handler to the list of running handlers
	 * with the current thread's name as a key
	 */
	private void addHandler() {
		logger.debug("Adding new HTTP handler: " + Thread.currentThread().getName());
		synchronized (runningHandlers) {
			runningHandlers.put( Thread.currentThread().getName(), this );  
		}
	}

	private void sendFile( InputStream stream ) throws HTTPException {
		try {
			response.sendFile(stream, getSessionObject(true));
		} catch (IOException e) {
			logger.error( e );
			throw new HTTPException( HTTPResponse.CODE_404, "Could not send stream" );
		}
	}

	private void sendFile( FileTreeNode target ) throws HTTPException {
		logger.debug("Object found is a File, attempting to serve static content");
		/*
		String p = file.getAbsolutePath();
		if (!p.endsWith(File.separator)){
			p += File.separator;
		}
		File target = new File( p + method );
		*/
		// logger.debug("Mapped static content reference to file: " + p + method);

		/* Make sure the file exists */
		if ( ! target.getFile().exists() ){
			throw new HTTPException( HTTPResponse.CODE_404, "No file found" );
		}

		/* Send the file */
		try {
			response.sendFile(target, request.getHeader(HTTPRequest.IF_MODIFIED_SINCE_HEADER), getSessionObject(true));
		} catch (IOException e) {
			logger.error(e);
			throw new HTTPException( HTTPResponse.CODE_404, "File could not be loaded" );
		}
	}

	/**
	 * Handles GET and POST requests, sorts out the
	 * querystring/form data and calls the appropriate map
	 * objects.
	 * @throws HTTPException 
	 * @throws ResponseCommittedException 
	 */
	private void handleGetPost() throws HTTPException, ResponseCommittedException {

		/* get an array of params whether get or post */
		Object[] requestParams = request.getRequestParameters();
		if ( requestParams == null && request.isPost() ){
			throw new HTTPException( HTTPResponse.CODE_400, "No form data found in the POST request");
		}

		/* check to see if a static file can be served */
		String urlPath = request.getPath();
		if ( serveStaticFile( urlPath ) ){
			return;
		}

		/*
		 * no static file matched, breakdown urlPath to object and the
		 * name of the method to call
		 */
		ObjectAndMethod call = request.getObjectAndMethod();
		String mappedPath = call.getObject();
		String method = call.getMethod();

		/*
		 * allow SecurityManager to veto the call if appropriate.
		 */
		try {
			String redirectAfterLogin = securityManager.checkRequest(request, getSession(true), logger);

			if (redirectAfterLogin != null) {
				response.setCookie( new Cookie( SecurityManager.LOGIN_REDIRECT_COOKIE_NAME, "delete", new Date(0), null, null, false));
				response.sendRedirect(redirectAfterLogin, request.isHttp11());
				return;
			}
		} catch (NotAuthenticatedException e) {
			logger.warn("User not authenticated: " + e.getMessage());
			authenticateUser();
			return;
		} catch (NotAuthorizedException e) {
			/* throw 403 */
			logger.warn( "User not authorized: " + e.getMessage() );
			throw new HTTPException( HTTPResponse.CODE_403, "Resource is protected");
		}

		// We use path as the key to look up the object to call
		logger.debug("Looking up object on path '" + mappedPath + "'");
		Object applicationObject = server.getRegisteredObject(mappedPath);
		if (applicationObject == null){
			throw new HTTPException( HTTPResponse.CODE_404, "No object mapping found for " + mappedPath);
		}

		logger.debug("Found object for path '" + mappedPath + "': " + applicationObject.toString());

		/* Is the object a File? If so, that becomes our directory
		 * and we serve the content straight out of there. Otherwise,
		 * we find and call the method with the correct name.
		 */
		/*
		if ( applicationObject instanceof File ){
			sendFile( applicationObject );
			return;
		}
		*/

		/* resolve the method on the application object that we need to call */
		Method methodToInvoke = getMethodToInvoke(requestParams, method, applicationObject);
		if (methodToInvoke == null){
			throw new HTTPException( HTTPResponse.CODE_404, "Page " + method + " not found in path " + mappedPath);
		}
		
		methodToInvoke.setAccessible( true );
		logger.debug( "Method is accessible = " + methodToInvoke.isAccessible() );

		Class[] classes = null;

		/* get a map of the parameter name/values */
		Map map = request.getRequestParametersMap();
		Object[] requestValues = null;

		boolean isMapMethod = methodToInvoke.getParameterTypes().length == 1 && methodToInvoke.getParameterTypes()[0] == Map.class;
		if ( isMapMethod ) {
			// ensure we don't call a method with a null map
			if (map == null){
				map = new HashMap();
			}
			classes = new Class[] { Map.class };
		} else if (requestParams != null) {
			/* If we have some args, create an array
			 * of appropriate classes for looking up the method
			 */
			logger.debug("Args found, creating a class array of length " + requestParams.length);
			classes = new Class[requestParams.length];
			for (int i = 0; i < classes.length; i++){
				classes[i] = requestParams[i].getClass();
			}

			/* populate an array of values */
			requestValues = map.values().toArray();
		}

		// Make the call    
		Object applicationResponse = null;
		try {

			if (isMapMethod) {
				logger.debug("Calling: " + methodToInvoke.toString());
				applicationResponse = methodToInvoke.invoke(applicationObject, new Object[] { map } );
				logger.debug("Received response: " + applicationResponse);
			} else {
				logger.debug("Calling: " + methodToInvoke.toString());
				applicationResponse = methodToInvoke.invoke(applicationObject, requestValues);
				logger.debug("Received response: " + applicationResponse);
			}
			// logger.info( "Received " + applicationResponse.getClass().toString() );
			if ( applicationResponse instanceof FileTreeNode ){
				sendFile((FileTreeNode) applicationResponse);
				return;
			} else if ( applicationResponse instanceof InputStream ){
				sendFile((InputStream) applicationResponse);
				return;
			}
		} catch (InvocationTargetException e) {
			/* underlying user method threw something */
			logger.error( e );
			throw new HTTPException( HTTPResponse.CODE_500, "An error occurred calling " + mappedPath + " " + method + "<br><br>Message was: " + e.getCause().getMessage() );
		} catch (Exception e) {
			logger.error( e );
			return;
		}

		if ( applicationResponse == null ){
			applicationResponse = "";
		}

		/* Has a response already been committed (ie. Did
		 * the method call send a file or a byte array?) If so,
		 * no point doing anything now.     
		 */
		if (! response.isResponseCommitted() ){
			/* Does the response from the object contain an HTTP response 
			 * code? if not, we'll stick on our default 200 OK
			 */
			if ( !applicationResponse.toString().startsWith( "HTTP" ) ){
				response.sendOk( applicationResponse.toString() );
			} else {
				response.sendRaw( applicationResponse.toString() );
			}
		}
	}

	/**
	 * @param requestParams
	 * @param method
	 * @param o
	 * @return
	 * @throws HTTPException
	 */
	private Method getMethodToInvoke(Object[] requestParams, String method, Object o) throws HTTPException {
		/* 
		 * This lowers efficiency but makes for more
		 * readable errors - we scan through the methods
		 * in the object to find ones with the name
		 * given and check the arg length manually.
		 * We can then say "method doesn't exist" or
		 * "method exists, but arguments are wrong"
		 */
		Method[] ms = o.getClass().getMethods();
		Method methodToInvoke = null;
		int arglength = 0;

		if (requestParams != null) arglength = requestParams.length;

		logger.debug("Searching for method '" + method + "' on Object " + o.toString());

		for (int i = 0; i < ms.length; i++) {
			if (ms[i].getName().equals(method)) {

				logger.debug("Found method with matching name '" + method + "'");

				// Does the method exist but accepting a Map?
				// If so, we can bundle up the arguments into it
				// instead of passing them as parameters
				if (ms[i].getParameterTypes().length == 1 &&
						ms[i].getParameterTypes()[0] == Map.class) {
					logger.debug("Found method with matching name accepting a Map");
				}
				else {
					// Check number of args matches
					if (ms[i].getParameterTypes().length != arglength)
						throw new HTTPException(
								HTTPResponse.CODE_500, 
								"Number of arguments did not match - expected " + 
								ms[i].getParameterTypes().length + " and got " + arglength + " instead.");
				}

				// No point continuing to look - we stipulate only
				// one method mapped to a URL as you can't overload a
				// URL!
				methodToInvoke = ms[i];
				break;
			}
		}
		return methodToInvoke;
	}

	private void authenticateUser() throws ResponseCommittedException {
		String loginUrl;
		if ((loginUrl = securityManager.getLoginUrl()) != null) {
			/* 
			 * application is using form based login.  Set a cookie
			 * pointing to the ORIGINAL request URL and redirect to
			 * login form
			 */
			logger.debug("Redirecting request to login form at [" + loginUrl + "]");
			String redirectTo = (String) request.getCookies().get(
					SecurityManager.LOGIN_REDIRECT_COOKIE_NAME);
			if (redirectTo == null)
				redirectTo = request.getUrl().getFile();

			response.setCookie(
					new Cookie(SecurityManager.LOGIN_REDIRECT_COOKIE_NAME, redirectTo, null, null, null, false));
			response.sendRedirect(loginUrl, request.isHttp11());

		} else        
			// send 401, realm is the app name
			response.sendNotAuthenticated(server.getApplicationName());
	}

	/* this is never used so comment it out */
	private boolean serveStaticFile(String urlPath) 
		throws ResponseCommittedException, HTTPException {
			/*
			 * Do we have an exact match for the URL (assuming it's not a
			 * directory request)? If so, it's got to be a static File - if
			 * it is, we serve it
			 */
			/*
			if (!urlPath.endsWith("/")) {
				Object em = server.getRegisteredObject(urlPath);
				if (em != null) {
					logger.debug("Found exact match for URL: " + urlPath);
					if (!(em instanceof File))
						throw new HTTPException(
								HTTPResponse.CODE_404,
								"The mapping for [" + urlPath + "] is an exact match, but not a file.");


					File target = (File) em;    
					logger.debug("Object found is a File, attempting to serve static content");
					logger.debug("Mapped static content reference to file: " + target.getAbsolutePath());
					// Make sure the file exists
					if (!target.exists())
						throw new HTTPException(
								HTTPResponse.CODE_404,
								"File does not exist");

					// Send the file
					try {
						response.sendFile( target, request.getHeader(HTTPRequest.IF_MODIFIED_SINCE_HEADER));
					} catch (IOException e) {
						logger.error(e);
					}
					return true;
				}
			}
			*/

			return false;
		}

	/**
	 * Clears up class level references
	 * before leaving.
	 */
	private void dispose() { 
		server = null;
	}
}
