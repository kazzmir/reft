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
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Properties;

import net.sf.ooweb.util.Logger;
import net.sf.ooweb.util.SimpleLogger;

/**
 * Configuration is an optional bootstrap class that can be used to load
 * a set of properties for your OOWeb application.  If you
 * specify a filename or File object it will use that, if it fails,
 * or you don't specify one, it will attempt to load config from the
 * default location - a file called ooweb.properties in the root of
 * the classpath.
 * 
 * This class will instantiate and configure a Logger based on your
 * property settings and this logger is used by the rest of the 
 * framework.
 * 
 * Configuration provides utility methods to extract properties of the
 * required type from the underlying table (getInt(), getLong() etc).
 *
 * @author Darren Davison
 * @since Jul 26, 2005
 */
public class Configuration {

    Properties props = new Properties();
    
    Logger logger;    

    /**
     * ctor looks in the root of the classpath for the file
     * ooweb.properties which it uses to configure the application.
     */
    public Configuration() {
        loadDefaults();
        if (logger == null)
            logger = new SimpleLogger();
        
    }

    /**
     * Tries to load properties from the specified location on the local
     * filesystem, falling back to checking for the file
     * ooweb.properties in the root of the classpath.
     */
    public Configuration(String propertiesLocation) {
        this(new File(propertiesLocation));
    }

    /**
     * Tries to load properties from the specified location on the local
     * filesystem, falling back to checking for the file
     * ooweb.properties in the root of the classpath.
     */
    public Configuration(File properties) {
        try {
            props.load(new FileInputStream(properties));
            System.out.println("Loaded configuration from file [" + properties.getAbsolutePath() + "]");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Attempting to load from default location instead..");
            // attempt default load
            loadDefaults();
        }
        
        configureLogger();
    }
    
    /**
     * Uses the supplied Properties instance to act as the underlying configuration
     * for this object.
     * 
     * @param props
     */
    public Configuration(Properties props) {
        this.props = props;
        configureLogger();
    }

    private void loadDefaults() {
        try {
            InputStream is = getClass().getResourceAsStream("/ooweb.properties");
            if (is != null) {
                props.load(is);
                System.out.println("Loaded configuration from default properties");
                configureLogger();
            } else
                System.out.println("Failed to load default properties.  I give up.");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void configureLogger() {
        if(logger == null) {
            Class clazz;
            String className = props.getProperty("logger.class", "net.sf.ooweb.util.SimpleLogger");
            try {
                clazz = Class.forName(className);
                Constructor c = clazz.getConstructor(new Class[] {Properties.class});
                logger = (Logger) c.newInstance(new Object[] {props});
            } catch (NoSuchMethodException e) {
                // use default ctor
                try {
                    logger = (Logger) Class.forName(className).newInstance();
                } catch (Exception e2) {
                    System.out.println(e2.getMessage() + " occurred configuring Logger. Using default logger");
                    logger = new SimpleLogger(props);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + " occurred configuring Logger. Using default logger");
                logger = new SimpleLogger(props);
            }
        }
        
    }
    
    /**
     * Utility method to return a property value of the correct type from the 
     * underlying table of properties.
     * 
     * @param propertyName the name of the property for which a value is required
     * @param defaultValue the value to return if the requested property name cannot
     * be resolved as an int or doesn't exist
     * @return an int value for the propertyName supplied
     */
    public int getInt(String propertyName, int defaultValue) {
        try {
            return(Integer.parseInt(props.getProperty(propertyName)));
        } catch (Exception e) {
            logger.debug("property [" + propertyName + "] is not an int.  Returning default.");
            return defaultValue;
        }
    }
    
    /**
     * Utility method to return a property value of the correct type from the 
     * underlying table of properties.
     * 
     * @param propertyName the name of the property for which a value is required
     * @param defaultValue the value to return if the requested property name cannot
     * be resolved as a double or doesn't exist
     * @return a double value for the propertyName supplied
     */
    public double getDouble(String propertyName, double defaultValue) {
        try {
            return(Double.parseDouble(props.getProperty(propertyName)));
        } catch (Exception e) {
            logger.debug("property [" + propertyName + "] is not a double.  Returning default.");
            return defaultValue;
        }
    }
    
    /**
     * Utility method to return a property value of the correct type from the 
     * underlying table of properties.
     * 
     * @param propertyName the name of the property for which a value is required
     * @param defaultValue the value to return if the requested property name cannot
     * be resolved as a long or doesn't exist
     * @return a long value for the propertyName supplied
     */
    public long getLong(String propertyName, long defaultValue) {
        try {
            return(Long.parseLong(props.getProperty(propertyName)));
        } catch (Exception e) {
            logger.debug("property [" + propertyName + "] is not a long.  Returning default.");
            return defaultValue;
        }
    }
    
    /**
     * Utility method to return a property value of the correct type from the 
     * underlying table of properties.  Exactly the same as calling 
     * props.getProperty(key, default) on the underlying property table.
     * 
     * @param propertyName the name of the property for which a value is required
     * @param defaultValue the value to return if the requested property name cannot
     * be found
     * @return a String value for the propertyName supplied
     */
    public String getString(String propertyName, String defaultValue) {
        return props.getProperty(propertyName, defaultValue);
    }

    /**
     * return a copy of the properties loaded by this Configuration.  Changing the
     * properties retrieved by this method will not affect the current configuration,
     * it is a copy only.
     *
     * @return the property set associated with this configuration (which
     * may be empty)
     */
    public Properties getProperties() {
        Properties p = new Properties();
        p.putAll((Map) props);
        return p;
    }
    
    /** 
     * return any configured Logger
     * 
     * @return the Logger associated with this Configuration
     */
    public Logger getLogger() {
        return logger;
    }

}
