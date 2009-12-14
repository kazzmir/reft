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
 * ObjectAndMethod is a simple value object holding the name of 
 * the mapped object and the required method to call on that
 * object.
 * 
 * @author Darren Davison
 * @since 0.5
 */
class ObjectAndMethod {

    private String object;
    
    private String method;
    
    public ObjectAndMethod() {
        super();
    }

    /**
     * @param method
     * @param object
     */
    public ObjectAndMethod(String method, String object) {
        super();
        this.method = method;
        this.object = object;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public void setObject(String object) {
        this.object = object;
    }

}
