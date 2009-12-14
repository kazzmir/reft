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
package net.sf.ooweb.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * WebUser implements the User interface to provide basic user
 * modelling.  This implementation can be used by application
 * code if no other functionality is needed from a User object.
 * 
 * @author Darren Davison
 * @since 0.5
 * @see User
 */
public class WebUser implements User {
    
    private String userName;
    
    private List roles = new ArrayList();

    public WebUser() {
        super();
    }

    /**
     * @see net.sf.ooweb.security.User#hasRole(java.lang.String)
     */
    public boolean hasRole(String roleName) {
        for (Iterator i = roles.iterator(); i.hasNext();)
            if (((String) i.next()).equals(roleName)) return true;
            
        return false;
    }
    
    /**
     * @param roleName
     */
    public void addRole(String roleName) {
        roles.add(roleName);
    }
    
    /**
     * @param roleNames
     */
    public void addRoles(List roleNames) {
        roles.addAll(roleNames);
    }
    
    /**
     * @return the List of roles this user has
     */
    public List getRoles() {
        return roles;
    }
    
    /**
     * @return the username
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}
