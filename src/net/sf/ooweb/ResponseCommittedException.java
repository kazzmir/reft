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


/**
 * ResponseCommittedException is thrown to indicate that something attempted
 * to send additional data to the HTTP client in response to a request, but
 * that the output stream had already been flushed and closed.
 * 
 * @author Darren Davison
 * @since 0.5
 */
public class ResponseCommittedException extends IOException {

    private static final long serialVersionUID = -9085499807850809035L;

    /**
     * default c'tor
     */
    public ResponseCommittedException() {
        super();
    }

    /**
     * @param s
     */
    public ResponseCommittedException(String s) {
        super(s);
    }

}
