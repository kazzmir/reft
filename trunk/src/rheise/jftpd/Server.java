package rheise.jftpd;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

import com.rafkind.reft.Lambda1;

import java.net.ServerSocket;
import java.net.Socket;

// import com.rafkind.reft.FileManager;
import com.rafkind.reft.FileTree;

import java.util.Properties;

/**
 * This is the main class for jftpd. It creates the server socket and loops
 * forever accepting user connections and creating ServerPI threads to handle
 * them.
 * <p>
 * To run under unix, type:
 *
 * <pre>java rheise.jftpd.Server</pre>
 */
public class Server{
	/**
	 * The version number of jftpd.
	 */
	public static final String VERSION = "Version 0.3";

	/**
	 * The port this server connects to.
	 */
	public static final int SERVER_PORT = 21;

	private List threads;

	private boolean on = false;

	/**
	 * The default data port.
	 */
	public static final int SERVER_DATA_PORT = 20;

	/**
	 * The port this server is listening on.
	 */
	private int port;
		
	private ServerSocket serverSocket;

	/*
	public static void main(String[] args) throws Exception {
			if (args.length > 1)
			{
				System.err.println("Usage: jftpd [ port ]");
				System.exit(1);
			}

			InputStream propertiesIn = Server.class.getResourceAsStream("jftpd.properties");
			if (propertiesIn == null)
				throw new FileNotFoundException("${archive}/rheise/jftpd/jftpd.properties");
			Properties properties = new Properties();
			properties.load(propertiesIn);

			int port = SERVER_PORT;
			if (args.length == 1)
				port = Integer.parseInt(args[0]);
			Server server = new Server(properties, port);

			// Start the FTP server as a standalone server.
			//
			server.start();
		}
		*/

	/**
	 * Create an FTP server to run on port 21.
	 */
	public Server( Properties properties ) throws FTPException {
		this(properties, SERVER_PORT);
	}

	/**
	 * Create an FTP server to run on the specified port.
	 */
	public Server( Properties properties, int port ) throws FTPException{
		try{
			configure( properties );
			serverSocket = new ServerSocket( port );
			threads = new ArrayList();
		} catch ( IOException ie ){
			throw new FTPException( "Could not start FTP server", ie );
		} catch ( ParseException pe ){
			throw new FTPException( "Could not load FTP properties", pe );
		}
	}

	/**
	 * Starts the FTP server. This method listens for connections on the
	 * FTP server port (usually port 21), and spawns a new thread to
	 * handle each connection.
	 */
	public void start( FileTree manager, final Lambda1 log ){
		on = true;
		while ( on ){
			try{
				Socket clientSocket = serverSocket.accept();
				ServerPI pi = new ServerPI( clientSocket, manager, log );
				Thread t = new KillableThread( pi );
				threads.add( t );
				t.start();
			} catch ( IOException ie ){
				ie.printStackTrace();
			}
		}
	}

	public void stop(){
		/* kill this thread */
		try{
			Lambda1.foreach( threads, new Lambda1(){
				public Object invoke( Object t ){
					KillableThread thread = (KillableThread) t;
					if ( thread.isAlive() ){
						try{
							thread.kill();
						} catch ( Exception e ){}
					}
					return null;
				}
			});
		} catch ( Exception e ){
		}

		System.out.println( "Killing the ftp server" );
		try{
			serverSocket.close();
		} catch ( Exception e ){
			e.printStackTrace();
		}
		on = false;
	}

	/**
	 * Handle a client connection. This may be called directly by a
	 * superserver instead of start().
	 *
	 * @param socket the client socket.
	 */
	/*
	public void service(Socket socket) throws Exception {
		ServerPI pi = new ServerPI(socket);
		pi.run();
	}
	*/

	/**
	 * Configures the ftp server from the configuration properties.
	 */
	private void configure(Properties properties) throws ParseException {
		String priorityStr = properties.getProperty("log.priority", "fatal" );
		if (priorityStr == null){
			throw new ParseException(ParseException.PROPERTY_UNDEFINED, "log.priority", null);
		}

		String priorityNames[] = Logger.priorityNames;
		int priority = Logger.LOG_OFF;
		for (int i = 0; i < priorityNames.length; i++){
			if (priorityNames[i].equals(priorityStr)){
				priority = i;
				break;
			}
		}

		Logger.setPriority(priority);

		String output = properties.getProperty("log.output");
		OutputStream outputStream = null;
		if (output == null || output.equals("stderr")) {
			outputStream = System.err;
		} else {
			try {
				outputStream = new FileOutputStream(output, true);
			} catch (IOException e) {
				throw new ParseException(ParseException.PROPERTY_INVALID, "log.output", output);
			}
		}
		Logger.setWriter(new PrintWriter(new OutputStreamWriter(outputStream), true));
	}
}
