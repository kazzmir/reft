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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.rafkind.reft.Lambda1;

import net.sf.ooweb.security.SecurityManager;
import net.sf.ooweb.sessions.SessionFactory;
import net.sf.ooweb.util.Logger;
import net.sf.ooweb.util.SimpleLogger;
import net.sf.ooweb.util.StringUtils;

/**
 * The basic HTTP server used by OOWeb. This class is responsible for setting up
 * the server socket and listening for connections, each of which it hands off
 * to a <code>Worker</code> thread.
 * <p>
 * Server's can be configured with a <code>Configuration</code> instance
 * (preferred), or manually by setting its various bean properties.
 * <p>
 * Your sitemap objects that generate pages from their methods are registered
 * with the Server using the <code>register(path, object)</code> method.
 * <p>
 * It is suggested that you gate OOWeb apps with Apache/mod_proxy/mod_rewrite
 * 
 * @author Robin Rawson-Tetley
 * @author Darren Davison
 */
public class Server {

	/** Default port to listen on */
	private static final int DEFAULT_PORT = 8080;

	/** Default address to bind to */
	private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

	/** Default socket timeout */
	private static final int DEFAULT_TIMEOUT = 5000;

	/** Default number of threads in the pool */
	private static final int DEFAULT_THREADS = 25;

	/**
	 * Default application name - used to set the session cookie key
	 */
	private static final String DEFAULT_APP_NAME = "OOWeb";

	/**
	 * The default number of connections to queue up at the serversocket before
	 * dropping them.
	 */
	private static final int SERVERSOCKET_BACKLOG = 50;

	private static final String QUIP_FILE = "quips.txt";

	private static final String DEFAULT_SHUTDOWN_PASSWORD = "secret";

	/** interface to bind to.. all by default */
	private String bindAddress = DEFAULT_BIND_ADDRESS;

	/**
	 * Faster than comparing strings - if the bindaddress is 0.0.0.0, we set
	 * this flag to not bother with address filtering.
	 */
	private boolean bindAll = false;

	private ServerSocket server;

	/** port to listen on at the address */
	private int port = DEFAULT_PORT;

	/** Timeout value for handling requests */
	private int timeout = DEFAULT_TIMEOUT;

	/** The maximum number of worker threads */
	private int maxThreads = DEFAULT_THREADS;

	/**
	 * The running application name. Used for the session tracking cookie.
	 */
	private String applicationName = DEFAULT_APP_NAME;

	/** The strategy (local or replicated) for handling sessions */
	private int sessionStrategy = SessionFactory.DEFAULT_STRATEGY;

	/** Collection of worker threads */
	private Vector threadpool = new Vector();

	/** password required for server shutdown */
	private String shutdownPassword = DEFAULT_SHUTDOWN_PASSWORD;

	/**
	 * The map of objects to URLs. Your keys should be URL suffix after the
	 * first / and your values obviously the objects with method names as paths.
	 */
	private HashMap map = new HashMap();

	/* When an unknown url comes in give the application a chance
	 * to dynamically register the path
	 */
	private Lambda1 dynamicRegister;

	private Logger logger;

	private static final List quipList = new ArrayList();

	private boolean keepRunning = true;

	static {
		// load quips from classpath, keeps them a bit
		// more manageable and still as well hidden :)
		InputStream stream = null;
		try {
			stream = Server.class.getResourceAsStream(QUIP_FILE);
			BufferedReader reader = new BufferedReader(new InputStreamReader( stream));
			String nextQuip;
			while ((nextQuip = reader.readLine()) != null)
				quipList.add(nextQuip);
		} catch (Exception ex) {
			// make sure we have at least one quip
			System.err.println("Failed to load quips: " + ex.getMessage());
			if (quipList.size() == 0)
				quipList.add("Ready to rock!");
		}
	}

	/**
	 * Creates a new server for your application.
	 * 
	 * @param cfg
	 *            a Configuration used to configure the server
	 */
	public Server( Configuration cfg ){
		logger = cfg.getLogger();

		setApplicationName(cfg.getString("applicationName", DEFAULT_APP_NAME));
		setBindAddress(cfg.getString("bindAddress", DEFAULT_BIND_ADDRESS));
		setPort(cfg.getInt("port", DEFAULT_PORT));
		setTimeout(cfg.getInt("timeout", DEFAULT_TIMEOUT));
		setMaxThreads(cfg.getInt("maxThreads", DEFAULT_THREADS));
		setShutdownPassword(cfg.getString("shutdownPassword",
				DEFAULT_SHUTDOWN_PASSWORD));
		setSessionStrategy(cfg.getInt("session.strategy", 
					SessionFactory.DEFAULT_STRATEGY));

		showBanner();
		allocateThreadPool();
        
		// security
		SecurityManager.getInstance().setProperties(cfg.getProperties());

		// server = new ServerSocket(port, SERVERSOCKET_BACKLOG, createInetAddressFromIP(getBindAddress()));

		dynamicRegister = new Lambda1(){
			public Object invoke( Object o ){
				return null;
			}
		};
	}

	public void startUp() throws IOException {
		server = new ServerSocket( getPort(), SERVERSOCKET_BACKLOG, createInetAddressFromIP( getBindAddress() ));
	}

	/**
	 * Creates a new server for your application.
	 * 
	 * @param applicationName
	 *            The application name - used for the session cookie
	 */
	/*
	public Server(String applicationName) {
		this.applicationName = applicationName;
		logger = new SimpleLogger();
		showBanner();
		allocateThreadPool();
	}
	*/

	/**
	 * Register an object to be used for a given path.
	 * 
	 * @param path
	 * @param object
	 */
	public void register(String path, Object object) {
		if (path == null || object == null)
			throw new IllegalArgumentException("Cannot register null object");
		if (!path.startsWith("/"))
			path = "/" + path;
		map.put(path, object);
	}

	/**
	 * Return the object associated with the supplied path
	 * 
	 * @param path
	 * @return the object keyed against the path or null
	 */
	public Object getRegisteredObject(String path) {
		Object o = map.get( path );
		if ( o == null && dynamicRegister != null ){
			// System.out.println( "Calling dynamic register: " + path );
			return dynamicRegister.invoke_( ("." + path).replaceAll( "%20", " " ).split( "/" ) );		
		}
		return o;
	}

	public void setDynamicRegister( Lambda1 dynamic ){
		this.dynamicRegister = dynamic;
	}

	/**
	 * Start the webserver listening on the configured address and port
	 */
	public void listen() {

		// Initialise the session implementation if required
		SessionFactory.initialiseSessions(logger, sessionStrategy);

		// Create and bind the server socket
		// ServerSocket server = null;
		
		keepRunning = true;
		logger.info(getQuip());
		while ( keepRunning ){
			try {
				Socket s = server.accept();
				Worker w = null;

				// If there isn't anything in the threadpool,
				// spawn a new thread.
				if (threadpool.isEmpty()) {
					w = new Worker();
					new Thread(w).start();
					w.go(this, s, logger);
				} else {
					w = (Worker) threadpool.elementAt(0);
					threadpool.removeElementAt(0);
					w.go(this, s, logger);
				}

			} catch (IOException e) {
				logger.error( e );
			}
		}
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Server (" + applicationName + ") listening on port " + port
				+ " at address " + bindAddress + "\nwith timeout " + timeout;
	}

	/**
	 * output a startup banner
	 */
	public void showBanner() {
		System.out
				.println("==========================================================================\n"
						+ this.toString()
						+ "\n==========================================================================");
	}

	/* bean mutators/accessors */
	public String getApplicationName() {
		return applicationName;
	}

	public String getBindAddress() {
		return bindAddress;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
		// NB: Deliberately didn't use DEFAULT_BIND_ADDRESS as that may
		// change to 127.0.0.1 in future for security reasons. 0.0.0.0
		// is the genuine "all" address.
		bindAll = bindAddress.equals("0.0.0.0");
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public void setSessionStrategy(int strategy) {
		this.sessionStrategy = strategy;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setShutdownPassword(String shutdownPassword) {
		this.shutdownPassword = shutdownPassword;
	}

	/**
	 * Creates and allocates the threads to the pool
	 */
	void allocateThreadPool() {
		for (int i = 0; i < maxThreads; ++i) {
			Worker w = new Worker();
			new Thread(w, "#" + i).start();
			threadpool.addElement(w);
		}
		logger.info("Pool of " + maxThreads + " threads created.");
	}

	/**
	 * handles returning a Worker to the pool if required
	 * 
	 * @param worker
	 */
	void returnThread(Worker worker) {
		if (threadpool.size() < maxThreads)
			threadpool.addElement(worker);
	}

	/**
	 * 
	 * @return the complete string required to effect a server shutdown.
	 *         Obviously not public.
	 */
	String getShutDownPassword() {
		return shutdownPassword;
	}

	/**
	 * Telnet into the server and issue the password to shutdown the server
	 * cleanly.
	 */
	public void shutdown() {
		this.keepRunning = false;
		try{
			server.close();
		} catch ( IOException e ){
			e.printStackTrace();
		}
	}

	/**
	 * Given an IPv4 address, generates an InetAddress object or null if the IP
	 * address is malformed
	 * 
	 * @param ip
	 *            The IP address
	 * @return An <code>InetAddress</code> object
	 */
	protected InetAddress createInetAddressFromIP(String ip) {
		String[] bits = StringUtils.split(ip, ".");
		if (bits.length != 4)
			return null;
		byte[] b = new byte[4];
		b[0] = Byte.parseByte(bits[0]);
		b[1] = Byte.parseByte(bits[1]);
		b[2] = Byte.parseByte(bits[2]);
		b[3] = Byte.parseByte(bits[3]);
		try {
			return InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
		}
		return null;
	}

	/**
	 * Returns a random quip for the startup message.
	 * 
	 * @return a quip
	 */
	protected String getQuip() {
		// Select one at random
		int quip = (int) (Math.random() * ((double) quipList.size()));
		return (String) quipList.get(quip);

	}

}
