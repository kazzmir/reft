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
package net.sf.ooweb.util;


/**
 * Logger is the public interface for OOWeb's lightweight logging
 * framework.  Any client code wishing to define their own logger
 * for use in OOWeb must implement this interface.
 * <p>
 * The simplest way to implement a custom logger is to extend the
 * class {@link AbstractLogger} and implement its abstract methods
 * instead.
 * 
 * @author Darren Davison
 * @since 0.3
 */
public interface Logger {

    // jdk 5 enums anyone?
    public static final int LOG_ERROR = 0;

    public static final int LOG_WARN = 1;

    public static final int LOG_INFO = 2;

    public static final int LOG_DEBUG = 3;

    /**
     * Logs a debug message
     * @param m The message
     */
    public void debug(String m);

    /**
     * Logs an info message
     * @param m The message
     */
    public void info(String m);

    /**
     * Logs a warning message
     * @param m The message
     */
    public void warn(String m);

    /**
     * Logs an error message
     * @param m The message
     */
    public void error(String m);

    /**
     * Logs an error message
     * @param t The throwable instance
     */
    public void error(Throwable t);
    
    /**
     * set the IP address of the client for inclusion 
     * in logging output
     * @param clientIpAddress
     */
    public void setClientIpAddress(String clientIpAddress);

}