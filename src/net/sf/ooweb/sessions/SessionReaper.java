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

import java.util.Vector;

import net.sf.ooweb.HTTP;
import net.sf.ooweb.util.Logger;


/**
 * Thread responsible for reaping expired sessions.
 * 
 * @author Robin Rawson-Tetley
 * @since 0.5
 */
public class SessionReaper extends Thread {

	/** A collection of active sessions */
	protected static Vector activeSessions = new Vector();
	/** Logger to use */
	protected Logger log = null;
	/** Amount of time the thread should sleep in between reap checks */
	protected final static int SLEEP = 60000;
	
	public SessionReaper(Logger log) {
		this.log = log;
	}
	
	public void run() {
		
		// Set the thread name for logging purposes
		setName("REAP");
		
		while (true) {

			log.debug("Session reaper thread reporting for duty.");
			
			for (int i = 0; i < activeSessions.size(); i++) {
				Session s = (Session) activeSessions.get(i);
				if (s.expired()) {
					log.info("Removing expired session: " + s.getName());
					// Drop it from the map of existing sessions in HTTPHandler
					HTTP.deleteSession(s.getName());
					s.dispose(); // dispose calls removeSession() 
								 // here so no need to do it.
				}
			}

			try {
				Thread.sleep(SLEEP);
			}
			catch (InterruptedException e) {
			}
			
		}
	}
	
	/** Adds a session to the active list */
	public static void addSession(Session s) {
		activeSessions.addElement(s);
	}
	
	/** Removes a session from the active list */
	public static void removeSession(Session s) {
		activeSessions.removeElement(s);
	}

}
