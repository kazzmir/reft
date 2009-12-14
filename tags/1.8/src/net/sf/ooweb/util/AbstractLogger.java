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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * AbstractLogger is a public superclass for the lightweight OOWeb
 * logging system.  Clients may create their own loggers by 
 * extending this class and implementing the two abstract methods
 * within: <code>logAsError(String)</code> and 
 * <code>logAsStandard(String)</code>.
 * <p>
 * All of the logic determining whether a message will be logged
 * happens prior to calling these two methods, so subclasses need
 * only concern themselves with what to do with the messages.
 * <p>
 * The inbuilt {@link SimpleLogger} logs error messages to 
 * <code>System.err</code> and all other messages to 
 * <code>System.out</code>.  You are strongly advised to 
 * create your own subclasses of <code>AbstractLogger</code> for
 * anything but trivial applications, and have the messages logged
 * to files or other devices.
 * 
 * @author Darren Davison
 * @since 0.3
 */
public abstract class AbstractLogger implements Logger {
    
    /** the date format that will be used if no other is specified */
    protected static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    /** determines the verbosity of the logging */
    private int logLevel = LOG_INFO;
    
    private DateFormat sdf;
    
    private String clientIpAddress = "0.0.0.0";
    
    
    /**
     * Default constructor.  Creates a <code>Logger</code> using
     * the <code>LOG_INFO</code> level and using the default date
     * format.
     */
    public AbstractLogger() {
        this(LOG_INFO, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Constructor used to configure the logger from a set of
     * <code>Properties</code>.  The following properties can 
     * be read:
     * <ul>
     *   <li><code>logger.level</code></li>
     *   <li><code>logger.dateformat</code></li>
     * </ul>
     * 
     * @param props the Properties instance containing logging
     * configuration options
     */
    public AbstractLogger(Properties props) {       
        String lev = props.getProperty("logger.level", "info");
        this.logLevel = LOG_INFO;
        if (lev.equalsIgnoreCase("error") || lev.equalsIgnoreCase("v")) this.logLevel = LOG_ERROR;
        if (lev.equalsIgnoreCase("warn") || lev.equalsIgnoreCase("vv")) this.logLevel = LOG_WARN;
        if (lev.equalsIgnoreCase("debug") || lev.startsWith("vvvv")) this.logLevel = LOG_DEBUG;          
    
        sdf = new SimpleDateFormat(props.getProperty("logger.dateformat", DEFAULT_DATE_FORMAT));
    }
    
    private AbstractLogger(int logLevel, String dateFormat) {
        this.logLevel = logLevel;
        sdf = new SimpleDateFormat((dateFormat == null ? DEFAULT_DATE_FORMAT : dateFormat));
    }
    
    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#setClientIpAddress(java.lang.String)
     */
    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }

    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#debug(java.lang.String)
     */
    public void debug(String m) {
        if (logLevel >= LOG_DEBUG) log(LOG_DEBUG, m);
    }

    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#info(java.lang.String)
     */
    public void info(String m) {
        if (logLevel >= LOG_INFO) log(LOG_INFO, m);
    }

    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#warn(java.lang.String)
     */
    public void warn(String m) {
        if (logLevel >= LOG_WARN) log(LOG_WARN, m);
    }

    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#error(java.lang.String)
     */
    public void error(String m) {
        log(LOG_ERROR, m);
    }

    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#error(java.lang.Throwable)
     */
    public void error(Throwable t) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter( result );
        t.printStackTrace( printWriter );
        log(LOG_ERROR, result.toString());
    }

    /* (non-Javadoc)
     * @see net.sf.ooweb.util.Logger#log(java.net.Socket, int, java.lang.String)
     */
    private void log(int type, String m) {
        String sType;
        switch (type) {
            case LOG_ERROR:
                sType = "ERROR";
                break;
            case LOG_WARN:
                sType = "WARN";
                break;
            case LOG_INFO:
                sType = "INFO";
                break;
            default:
                sType = "DEBUG";
        }
        
        // Construct the time
        String time = sdf.format(new Date());
        
        // Log it (quickly)
        StringBuffer sb = new StringBuffer(256);
        sb.append(time).append(" ")
            .append(Thread.currentThread().getName()).append(" [")
            .append(sType).append("] ")
            .append(clientIpAddress).append(" ")
            .append(m);
        
        if (type == LOG_ERROR)
            logAsError(sb.toString());
        else
            logAsStandard(sb.toString());
    }
    
    /**
     * Log this message to the desired target (stderr or a file or socket etc).  Called
     * for all log messages of type LOG_ERROR
     * 
     * @param formattedMessage the message to log
     */
    protected abstract void logAsError(String formattedMessage);
    
    /**
     * Log this message to the desired target (stdout or a file or socket etc).  Called
     * for all log messages of type > LOG_ERROR (ie LOG_WARN, LOG_INFO, LOG_DEBUG)
     * 
     * @param formattedMessage the message to log
     */
    protected abstract void logAsStandard(String formattedMessage);

}
