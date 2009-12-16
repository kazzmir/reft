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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.ooweb.util.Logger;
import net.sf.ooweb.util.MimeType;
import net.sf.ooweb.util.StringUtils;
import net.sf.ooweb.sessions.Session;

import com.rafkind.reft.FileTreeNode;

/**
 * HTTPResponse manages output to the user agent including stream
 * resources.
 * 
 * @author Darren Davison
 * @since 0.5
 */
class HTTPResponse {

	// see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
	public static final String CODE_200 = "200 OK";
	public static final String CODE_302 = "302 Found";
	public static final String CODE_303 = "303 See Other";
	public static final String CODE_304 = "304 Not Modified";
	public static final String CODE_400 = "400 Bad Request";
	public static final String CODE_401 = "401 Authorization Required";
	public static final String CODE_403 = "403 Not Authorized";
	public static final String CODE_404 = "404 Not Found";
	public static final String CODE_500 = "500 Internal Server Error";

	private static final String DEFAULT_MIME_TYPE = "text/html";

	private static final DateFormat RFC2616_DATE_FORMAT = 
		new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z");

	private String mimeType = DEFAULT_MIME_TYPE;


	private StringBuffer resp = new StringBuffer();

	private List responseCookies = new LinkedList();

	/** The output stream on the socket */
	private PrintStream printStream = null;

	/** Logger to use for logging (!) */
	private Logger logger;

	private boolean committed = false;

	private boolean sessionCookieRequired;

	private String applicationName;


	HTTPResponse(String applicationName, PrintStream ps, Logger logger) {
		this.applicationName = applicationName;
		this.printStream = ps;
		this.logger = logger;
	}

	/**
	 * Test if the response has already been sent.
	 * @return true if the response has been committed.
	 */
	boolean isResponseCommitted() {
		return committed;
	}

	/**
	 * Adds a cookie to be sent back with the response
	 * @param cookie the Cookie to add
	 */
	void setCookie(Cookie cookie) {
		responseCookies.add(cookie);
	}

	/**
	 * determines whether the response will have a session cookie
	 * added at the time it is committed
	 * 
	 * @param b true if a session cookie should be sent, false
	 * otherwise
	 */
	void setSessionCookieRequired(boolean b) {
		sessionCookieRequired = b;
	}

	/**
	 * user methods can call HTTP.setMimeType() which delegates here so 
	 * that only the user's thread is affected for the specific method call.
	 * 
	 * @param mimeType
	 */
	void setMimeType(String mimeType) {
		this.mimeType  = mimeType;
	}    

	/**
	 * Sends an arbitrary array of bytes as the
	 * response. Marks the response as committed once sent.
	 * @param b The byte array to send
	 * @throws IOException if a problem occurs
	 */
	void sendByteArray(byte[] b, String mimeType) throws IOException {

		logger.debug("Sending byte array (" + b.length + " bytes)");

		// Send header
		this.mimeType = mimeType;
		sendOk();
		addToResponse("");
		logger.debug("Sending response: " + resp);
		printStream.print(resp.toString());
		printStream.flush();

		// Write the byte array body
		printStream.write(b);
		printStream.flush();
		committed = true;
	}
	
	protected String contentLength( File target ){
		logger.debug( "Requesting content length" );
		return "Content-Length: " + target.length();
	}

    protected void writeStream(InputStream stream, Session session, SpeedWatcher watcher) throws IOException {
        int n;
        byte[] fileBuffer = new byte[2048];
        watcher.print();
        while (session.isAlive() && (n = stream.read(fileBuffer)) > 0) {
            session.touch();
            printStream.write(fileBuffer, 0, n);
            watcher.update(n);
        }
        watcher.update(0);
    }

	void sendFile( File target, final String name, String ifModSince, Session session ) throws IOException {
		long fileMod = target.lastModified();    
		logger.debug("Client sends If-Modified-Since of " + ifModSince);

		if (false && ifModSince != null)
			try {
				long modSince = RFC2616_DATE_FORMAT.parse(ifModSince).getTime();
				logger.debug("File mod time is [" + fileMod + 
						"], if-modified-since time is [" + modSince + "]");
				if (fileMod <= modSince) {
					sendNotModified();
					return;
				}

			} catch (ParseException e) {
				logger.warn("Client sends unparseable date in If-Modified-Since header");
			}

		logger.debug("Sending file to client.  No If-Modified-Since, or file is modified");
		String lastModified = 
			RFC2616_DATE_FORMAT.format(new Date(fileMod));


		InputStream is = new FileInputStream( target.getAbsolutePath() );

		if ( mimeType.equals( DEFAULT_MIME_TYPE ) ){
			mimeType = MimeType.getMIMEType(target);
		}
		logger.debug( "Mime type set to " + mimeType );
		sendOk();
		// resp.append( "Content-Location: " + target.getName() + "\r\n" );
		addToResponse( "Expires: 0" );
		addToResponse( "Content-Transfer-Encoding: binary;" );
		addToResponse( "Cache-Control: must-revalidate, post-check=0, pre-check=0" );
		// addToResponse( "Content-Type: application/octet-stream" );
		addToResponse( "Content-Disposition: attachment; filename=" + name );
		addToResponse( contentLength( target ) );
		addToResponse("Last-Modified: " + lastModified);
		addToResponse("");
		logger.debug( "Sending response: " + resp );
		printStream.print(resp.toString());
		printStream.flush();

		try {
            final double length = target.length();
			writeStream(is, session, new SpeedWatcher(1000 * 10){
                public void print(){
                    logger.info(name + " " + com.rafkind.reft.Reft.reduce((this.totalBytes() * 100 / length)) + "% Current speed " + com.rafkind.reft.Reft.niceSize(this.currentSpeed()) + "/s");
                }
            });
		} finally {
			printStream.flush();
			is.close();
			is = null;
			committed = true;
		}
	}

	void sendFile( InputStream stream, Session session ) throws IOException {


		// logger.debug("Sending file " + target.getAbsolutePath() + " as " + Xtarget.getName() );

		/*
		   if ( mimeType.equals( DEFAULT_MIME_TYPE ) ){
		   mimeType = MimeType.getMIMEType(target);
		   }
		   */

		logger.debug( "Mime type set to " + mimeType );
		sendOk();
		// resp.append( "Content-Location: " + target.getName() + "\r\n" );
		addToResponse( "Expires: 0" );
		addToResponse( "Content-Transfer-Encoding: binary;" );
		// addToResponse( "Cache-Control: must-revalidate, post-check=0, pre-check=0" );
		// addToResponse( "Content-Type: application/octet-stream" );
		// addToResponse( "Content-Disposition: attachment; filename=" + Xtarget.getName().replaceAll( " ", "_" ) );
		// addToResponse( "Content-Length: -1" );
		addToResponse("");
		logger.debug( "Sending response: " + resp );
		printStream.print(resp.toString());
		printStream.flush();

		try {
			writeStream(stream, session, new SpeedWatcher());
			/*
			int n;
			byte[] fileBuffer = new byte[ 1 << 13 ];
			while ((n = stream.read(fileBuffer)) > 0) {
				printStream.write(fileBuffer, 0, n);
				if ( printStream.checkError() ){
					break;
				}
			}
			*/
		} finally {
			printStream.flush();
			stream.close();
			committed = true;
		}
	}

	/**
	 * @param target
	 * @param ifModSince
	 * @throws IOException
	 */
	void sendFile( FileTreeNode node, String ifModSince, Session session ) throws IOException {
		sendFile( node.getFile(), node.getName().replaceAll( " ", "_" ), ifModSince, session );
	}

	/**
	 * @param responseBody
	 * @throws ResponseCommittedException
	 */
	void sendOk(String responseBody) throws ResponseCommittedException {
		sendOk();
		// Send the response
		addToResponse(responseBody);
		commitResponse();   
	}

	/**
	 * @throws ResponseCommittedException
	 */
	private void sendOk() throws ResponseCommittedException {
		startResponse(HTTPResponse.CODE_200, (mimeType != null ? mimeType : DEFAULT_MIME_TYPE));
		if(sessionCookieRequired)
			responseCookies.add(new Cookie(applicationName, StringUtils.generateUUID()));
		addCookiesToResponse();
		if (mimeType == null || mimeType.equals(DEFAULT_MIME_TYPE))
			addToResponse("");
	}

	void sendRaw(String responseHeadersAndBody) throws ResponseCommittedException {
		if (committed)
			throw new ResponseCommittedException(
					"Response has already been committed.  Better luck next time!");     
		addToResponse(responseHeadersAndBody);
		commitResponse();
	}

	/**
	 * @param url the location to re-direct to
	 * @param http11 whether the response should be HTTP 1.1 compatible
	 * @throws ResponseCommittedException
	 */
	void sendRedirect(String url, boolean http11) throws ResponseCommittedException {
		String respCode = CODE_302;
		if (http11)
			respCode = CODE_303;

		startResponse(respCode, DEFAULT_MIME_TYPE);
		addToResponse("Location: " + url);
		addCookiesToResponse();

		// some clients don't like empty payloads in 302's
		addToResponse("");
		addToResponse("<html><body><h1>" + respCode + "</h1></body></html>");
		commitResponse();
	}

	/**
	 * @param httpEx the HTTPException causing the problem
	 * @param applicationPage rendered content ready to flush or 
	 * null if client app chose not to handle errors themselves.
	 */
	void sendError(HTTPException httpEx, String applicationPage) {
		String responseCode = httpEx.getHttpCode();
		String defaultPage = 
			"<html><body><h1>" + responseCode + "</h1><p>" + 
			httpEx.getMessage() + "</p><hr/><p>OOWeb Server</body></html>";

		logger.warn(httpEx.getMessage());

		try {
			startResponse(responseCode, DEFAULT_MIME_TYPE);
			addToResponse("");
			addToResponse(applicationPage == null ? defaultPage : applicationPage);
			commitResponse();

		} catch (ResponseCommittedException e) {
			// fatal
			logger.error(e);
		}
	}

	/**
	 * @throws ResponseCommittedException
	 */
	void sendNotModified() throws ResponseCommittedException {
		startResponse(CODE_304, DEFAULT_MIME_TYPE);
		logger.debug("Sending " + HTTPResponse.CODE_304 + " for file resource");
		commitResponse();
	}

	/**
	 * @param realm
	 * @throws ResponseCommittedException
	 */
	void sendNotAuthenticated(String realm) throws ResponseCommittedException {
		startResponse(CODE_401, DEFAULT_MIME_TYPE);
		logger.debug("Sending " + HTTPResponse.CODE_401 + " for resource");
		addToResponse("WWW-Authenticate: Basic realm=\"" + realm + "\"");
		commitResponse();
	}

	/**
	 * @param responseCode
	 * @param mimeType
	 * @throws ResponseCommittedException
	 */
	void startResponse(String responseCode, String mimeType) throws ResponseCommittedException {
		if (committed)
			throw new ResponseCommittedException(
					"Response has already been committed.  Better luck next time!");
		addToResponse("HTTP/1.1 " + responseCode);
		addToResponse("Content-Type: " + mimeType);
		addToResponse("Server: OOWeb");
	}

	private void addToResponse(String s) {
		if ( s != null ){
			resp.append(s).append("\r\n");
		}
	}

	private void addCookiesToResponse() {
		if (responseCookies.size() > 0)            
			for (Iterator i = responseCookies.iterator(); i.hasNext();) {
				Cookie c = (Cookie) i.next();
				addToResponse(c.toString());
			}
	}

	/**
	 * Flush the output streams and set a marker to show that no further
	 * responses can be made.
	 */
	void commitResponse() {
		logger.debug("Sending response: " + resp);
		printStream.print(resp.toString());
		printStream.flush();
		mimeType = null;
		committed = true;
	}

}
