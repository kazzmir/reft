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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

import net.sf.ooweb.util.Logger;

/**
 * Worker thread implementation for serving requests
 * 
 * @author Robin Rawson-Tetley
 */
class Worker implements Runnable {

    private Server srv = null;
    private Socket s = null;
    private Logger logger;
    
    Worker() {}
    
    /**
     * Main method to kick the thread off, notifies it
     * that it has to wake up and do some work.
     * 
     * @param srv
     * @param s
     * @param logger
     */
    void go(Server srv, Socket s, Logger logger) {
        this.srv = srv; 
        this.s = s;
        this.logger = logger;
        logger.setClientIpAddress(s.getInetAddress().getHostAddress());
        
        synchronized (this) {
            notify();
        }
    }

    public void run() {
        while(true) {
            if (s == null) {
                /** No socket - no work */
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    continue;
                }
            }
            
            try {
		Thread.currentThread().setName( s.getInetAddress().getHostAddress() );
                handleClient(srv, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            s = null;
            
            /**
             * Offer to return this thread to the pool.
             */
            srv.returnThread(this);
            logger = null;
            srv = null;
        }
    }
    
    /**
     * Handles the client request.
     * @param srv The SimpleWeb server
     * @param s The socket
     * @throws IOException
     */
    void handleClient(Server srv, Socket s) {

        InputStream is = null;
        PrintStream ps = null;

        try {

            logger.debug("Setting request timeout to: " + srv.getTimeout() + "ms");
            s.setSoTimeout(srv.getTimeout());
            s.setTcpNoDelay(true);
            
            // Get reader/writer on stream
            logger.debug("Creating IO streams");
            is = s.getInputStream();
            ps = new PrintStream(s.getOutputStream());

            HTTPRequest req = new HTTPRequest(srv.getApplicationName(), is, logger);
            HTTPResponse resp;
	    if ( req.isGet() ){
		    logger.debug( "Response is GET" );
		    resp = new HTTPResponse(srv.getApplicationName(), ps, logger);
	    } else {
		    logger.debug( "Response is HEAD" );
		    resp = new HTTPHeadResponse(srv.getApplicationName(), ps, logger);
	    }
            new HTTPHandler(srv, req, resp, logger);

        } catch (Exception e) {
            // I would say throw a 500 here, but that's not really
            // possible since if we got here, something is wrong
            // with the socket/streams
            logger.error(e);
            
        } finally {
            logger.debug("Closing socket and streams.");
            try {
                is.close();
            } catch (Exception e) {}
            try {
                ps.close();
            } catch (Exception e) {}
            try {
                s.close();
            } catch (Exception e) {}
            is = null;
            ps = null;
        }
    }
    
}
