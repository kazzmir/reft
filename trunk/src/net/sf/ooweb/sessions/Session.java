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
package net.sf.ooweb.sessions;

import java.util.HashMap;
import java.util.Map;

import net.sf.ooweb.util.Logger;

/**
 * Base class for sessions used by OOWeb. Provides a simple
 * implementation based around HashMap.
 * 
 * @author Robin Rawson-Tetley
 * @since 0.5
 */
public class Session {

	protected Map mapImpl = null;
	protected Logger logger = null;
	protected long lastTouched = System.currentTimeMillis();
	protected String name = "Session";
        private boolean alive = true;
	
	/** Default timeout value for sessions (600000ms = 10 minutes) */
	protected static final long TIMEOUT = 600000; 
	
	/** Creates a session with an optional name */
	public void create(String name) {
		mapImpl = new HashMap();
		this.name = name;
                alive = true;
	}
	
	/** Disposes of the session */
	public void dispose() {
		SessionReaper.removeSession(this);
		mapImpl = null;
                synchronized(this){
                    alive = false;
                }
	}

        public synchronized boolean isAlive(){
            return alive;
        }
	
	/** "Touches" the session to prevent it expiring */
	public void touch() {
		lastTouched = System.currentTimeMillis();
	}
	
	/** Checks whether the session has expired based on session timeout value */
	public boolean expired() {
		return System.currentTimeMillis() - lastTouched >= TIMEOUT;
	}
	
	/** Returns the last time (since epoch) the session was touched */
	public long getLastTouched() {
		return lastTouched;
	}
	
	/** Gets the Map implementation for this session */
	public Map getMap() {
		return mapImpl;
	}
	
	/** Sets the Map implementation for this session */
	public void setMap(Map m) {
		mapImpl = m;
	}
	
	/**
	 * Returns the logger being used by this Session
	 */
	public Logger getLogger() {
		return logger;
	}
	
	/** Sets the logger to use for outputting data */
	public void setLogger(Logger l) {
		logger = l;
	}

	public String getName() {
		return name;
	}
	
}
