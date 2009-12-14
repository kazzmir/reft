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

import java.util.Properties;



/**
 * SimpleLogger.  Uses stderr / stdout
 * 
 * @author Darren Davison
 * @since 0.5
 */
public class SimpleLogger extends AbstractLogger {
    
    public SimpleLogger() {
        super();
    }
    
    public SimpleLogger(Properties props) {     
        super(props);
    }
    
    /**
     * Prints the formatted message to standard error stream
     * 
     * @see net.sf.ooweb.util.AbstractLogger#logAsError(java.lang.String)
     */
    protected void logAsError(String formattedMessage) {
        System.err.println(formattedMessage);
    }

    /**
     * Prints the formatted message to standard output stream
     * 
     * @see net.sf.ooweb.util.AbstractLogger#logAsStandard(java.lang.String)
     */
    protected void logAsStandard(String formattedMessage) {
        System.out.println(formattedMessage);       
    }

}
