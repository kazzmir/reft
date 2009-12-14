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

import net.sf.ooweb.util.Logger;

/**
 * Simple factory class to allow switching between
 * replicated and regular sessions.
 * 
 * @author Robin Rawson-Tetley
 * @since 0.5
 */
public class SessionFactory {
    
    // permit other future strategies
    public static final int REPLICATED_STRATEGY = 0;
    
    public static final int DEFAULT_STRATEGY = 1;
    
    private static int sessionStrategy = DEFAULT_STRATEGY;
	private static Logger logger = null;
	private static SessionReaper reaper = null;
	
	/** Perform any startup necessary for sessions
	 *  and stores a reference to the logger being
	 *  used.
	 */
	public static void initialiseSessions(Logger l, int s) {
		logger = l;
        sessionStrategy = s;
        
		if (s == REPLICATED_STRATEGY) {
			// Start the replicated hashtable listening
			ReplicatedHashtable.log = l;
			ReplicatedHashtable.announceMe();
		}
		
		// Start the session reaping thread up
		reaper = new SessionReaper(l);
		reaper.start();
		
	}
	
	/** Creates a session object */
	public static Session createSession() {
		Session s = null;
		switch (sessionStrategy) {
            case REPLICATED_STRATEGY:
			s = new ReplicatedSession();
            break;
            
            default:
			s = new Session();
            
		}
		s.setLogger(logger);
		SessionReaper.addSession(s);
		return s;
	}

}
