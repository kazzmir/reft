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



/**
 * HTTPException holds an HTTP error code in addition to the message and an
 * optional root cause exception.  Thrown to indicate an error response of
 * some kind is required.
 * 
 * @author Darren Davison
 * @since 0.6
 */
public class HTTPException extends Exception {

    private static final long serialVersionUID = 138229532169061161L;

    private String httpCode;

    /**
     * @param httpCode
     * @param message
     * @param cause
     */
    public HTTPException(String httpCode, String message, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
    }

    /**
     * @param httpCode
     * @param message
     */
    public HTTPException(String httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }
    
    /**
     * @return the HTTP error code associated with this exception -
     * including the description
     */
    public String getHttpCode() {
        return httpCode;
    }

}
