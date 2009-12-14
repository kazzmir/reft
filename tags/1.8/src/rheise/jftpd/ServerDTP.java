package rheise.jftpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.net.ConnectException;
import java.net.Socket;

// import com.rafkind.reft.FileManager;
import com.rafkind.reft.FileTree;
import com.rafkind.reft.FileTreeNode;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

/**
 * This is the server data transfer process. It is responsible for
 * transferring files to and from the client. A separate data socket is
 * created to transfer the data.
 */
public class ServerDTP{
	/**
	 * The ServerPI that uses this DTP.
	 */
	private ServerPI serverPI;

	/**
	 * The host of the data socket.
	 */
	// private String dataHost;

	private Lambda0 getClientSocket;

	/**
	 * The port of the data socket.
	 */
	// private Integer dataPort;

	/**
	 * The transmission mode to be used. The initial transmission mode is
	 * STREAM mode.
	 */
	private TransmissionMode transmissionMode = TransmissionMode.STREAM;

	/**
	 * The representation being used for transmission. The initial
	 * representation type is ASCII.
	 */
	private Representation representation = Representation.ASCII;

	private FileTree manager;

	/**
	 * Creates a server data transfer process for the specified ServerPI.
	 */
	public ServerDTP( ServerPI serverPI, FileTree manager ){
		this.serverPI = serverPI;

		this.manager = manager;

		// this.dataPort = new Integer( -1 );

		this.getClientSocket = new Lambda0(){
			public Object invoke() throws Exception {
				throw new CommandException( 500, "Issue PORT or PASV" );
			}
		};

		/*
		this.closeSocket = new Lambda1(){
			public Object invoke( Object socket ) throws Exception {
				Socket s = (Socket) socket;
				s.close();
				return null;
			}
		};
		*/
	}

	/**
	 * Sets the transmission mode.
	 */
	public void setTransmissionMode(TransmissionMode transmissionMode) {
		this.transmissionMode = transmissionMode;
	}

	/**
	 * Sets the structure.
	 */
	public void setDataStructure(char stru) {
		// Ignore. Java itself only supports file-structure, so there
		// is no sense adding record-structure support in the server.
	}

	public void setClientSocket( Lambda0 get ){
		this.getClientSocket = get;
	}

	/*
	public void setCloseSocket( Lambda1 close ){
		this.closeSocket = close;
	}
	*/

	/**
	 * @return the representation type used for transmission.
	 */
	public Representation getRepresentation(){
		return representation;
	}

	/**
	 * Sets the representation type used for transmission.
	 */
	public void setRepresentation(Representation representation){
		this.representation = representation;
	}

	/**
	 * Sets the data port for an active transmission.
	 *
	 * @param host the host name to connect to.
	 * @param port the port number to connect to.
	 */
	/*
	public void setDataPort(String host, int port){
		dataHost = host;
		dataPort = new Integer( port );
	}
	*/

	/**
	 * Opens the data connection, reads the data according to the current
	 * transmission mode, representation type and structure, and writes it
	 * into the local file "path".
	 */
	public int receiveFile(String path) throws CommandException {
		int reply = 0;
		FileOutputStream fos = null;
		Socket dataSocket = null;
		try{
			File file = new File(path);
			if (file.exists())
				throw new CommandException(550, "File exists in that location.");

			fos = new FileOutputStream(file);

			// Connect to User DTP.
			//
			/*
			if (dataPort == -1)
				throw new CommandException(500, "Can't establish data connection: no PORT specified.");
			*/
			// dataSocket = new Socket(dataHost, dataPort);
			dataSocket = (Socket) getClientSocket.invoke();

			// Read file contents.
			//
			serverPI.reply(150, "Opening " + representation.getName() + " mode data connection.");
			transmissionMode.receiveFile(dataSocket, fos, representation);
			reply = serverPI.reply(226, "Transfer complete.");
		} catch (ConnectException e) {
			throw new CommandException(425, "Can't open data connection.");
		} catch (IOException e) {
			throw new CommandException(550, "Can't write to file");
		} catch ( Exception e ){
			throw new CommandException(550, "Unknown exception" );
		} finally {
			try{
				if (fos != null)
					fos.close();
				// closeSocket.invoke( dataSocket );
				if (dataSocket != null)
					dataSocket.close();
			} catch ( IOException e ) {
			} catch ( Exception e ){
			}
		}
		return reply;
	}

	/**
	 * Opens the data connection reads the specified local file and writes
	 * it to the data socket using the current transmission mode,
	 * representation type and structure.
	 */

	public void sendFile( String directory, String path ) throws CommandException {
		FileInputStream input = null;
		Socket socket = null;
		// System.out.println( "Send file " + directory + "/" + path );
		try{
			File file = manager.getFile( directory, path );
			if ( file == null ){
				throw new CommandException( 550, "No such file: " + directory + "/" + path );
			}
			input = new FileInputStream( file );
			socket = (Socket) getClientSocket.invoke();
			serverPI.reply( 150, "Opening " + representation.getName() + " mode data connection." );
			transmissionMode.sendFile( input, socket, representation );
			serverPI.reply(226, "Transfer complete.");
		} catch (ConnectException e) {
			throw new CommandException( 425, "Could not establish connection" );
		} catch ( IOException ie ){
			throw new CommandException( 550, "No such file" );
		} catch ( Exception e ){
			e.printStackTrace();
			throw new CommandException( 550, "Could not send file", e );
		} finally {
			try{
				if ( input != null ){
					input.close();
				}
				if ( socket != null ){
					socket.close();
				}
			} catch ( IOException ie ){
				ie.printStackTrace();
			}
		}
	}

	/* dont need this
	public void sendSize( String path ) throws CommandException {
		Socket socket = null;
		try{
			File file = manager.getFile( path );
			if ( file == null ){
				throw new CommandException( 550, "No such path" );
			}
			socket = (Socket) getClientSocket.invoke( dataHost, dataPort );
			Representation representation = Representation.ASCII;
			final PrintWriter writer = new PrintWriter( representation.getOutputStream( socket ) );

			serverPI.reply(150, "Opening " + representation.getName() + " mode data connection.");
			
			writer.print( "total " + numFiles + "\n" );

			// System.out.println( "Total " + numFiles );

			try{
				Lambda1.foreach( files, new Lambda1(){
					public Object invoke( Object str ){
						listFile( (File) str, writer );
						return null;
					}
				});
			} catch ( Exception e ){
				e.printStackTrace();
			}

			writer.flush();
			serverPI.reply(226, "Transfer complete.");
		} catch (ConnectException e) {
			throw new CommandException(425, "Can't open data connection.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommandException(550, "No such directory.");
		} finally {
			try{
				if ( socket != null ){
					socket.close();
				}
			} catch (IOException e){
			} catch ( Exception e ){
			}
		}

	}
	*/

	/**
	 * Sends a list of file names to the User DTP. Each line contains only
	 * the file name, not modification dates and file sizes (see
	 * sendList).
	 *
	 * @param path the path of the directory to list.
	 */
	public int sendNameList(String path) throws CommandException {
		int reply = 0;
		Socket dataSocket = null;
		try{
			List files = manager.getFileList( path );
			if ( files == null ){
				throw new CommandException( 550, "No such directory" );
			}

			dataSocket = (Socket) getClientSocket.invoke();
			Representation representation = Representation.ASCII;
			final PrintWriter writer = new PrintWriter(representation.getOutputStream(dataSocket));

			serverPI.reply(150, "Opening " + representation.getName() + " mode data connection.");
			try{
				Lambda1.foreach( files, new Lambda1(){
					public Object invoke( Object str ){
						FileTreeNode f = (FileTreeNode) str;
						writer.print( f.getName() );
						writer.print( "\n" );
						return null;
					}
				});
			} catch ( Exception e ){
				e.printStackTrace();
			}

			writer.flush();
			reply = serverPI.reply(226, "Transfer complete.");
		} catch (ConnectException e) {
			throw new CommandException(425, "Can't open data connection.");
		} catch (Exception e) {
			throw new CommandException(550, "No such directory.");
		} finally {
			try{
				if (dataSocket != null)
					dataSocket.close();
			} catch (IOException e) {
			}
		}

		return reply;
	}

	/**
	 * Sends a list of files in the specified directory to the User DTP.
	 * Each line contains the file name, modification date, file size and
	 * other information.
	 *
	 * @param path the path of the directory to list.
	 */
	public void sendList( final String path ) throws CommandException {
		Socket socket = null;
		try{
			List files = manager.getFileList( path );
			if ( files == null ){
				throw new CommandException( 550, "No such path" );
			}
			int numFiles = files.size();

			socket = (Socket) getClientSocket.invoke();
			Representation representation = Representation.ASCII;
			final PrintWriter writer = new PrintWriter( representation.getOutputStream( socket ) );

			serverPI.reply(150, "Opening " + representation.getName() + " mode data connection.");
			
			writer.print( "total " + numFiles + "\n" );

			try{
				Lambda1.foreach( files, new Lambda1(){
					public Object invoke( Object str ){
						listFile( (FileTreeNode) str, writer );
						return null;
					}
				});
			} catch ( Exception e ){
				e.printStackTrace();
			}

			/*
			for (int i = 0; i < numFiles; i++){
				String fileName = fileNames[i];
				File file = new File(dir, fileName);
				listFile(file, writer);
			}
			*/

			writer.flush();
			serverPI.reply(226, "Transfer complete.");
		} catch (ConnectException e) {
			throw new CommandException(425, "Can't open data connection.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommandException(550, "No such directory.");
		} finally {
			try{
				if ( socket != null ){
					socket.close();
				}
			} catch (IOException e){
			} catch ( Exception e ){
			}
		}
	}

	/*
	public int sendList(String path) throws CommandException {

		System.out.println( "List files for path '" + path + "'" );
		int reply = 0;
		Socket dataSocket = null;
		try{
			File dir = new File(path);
			String fileNames[] = dir.list();
			int numFiles = fileNames != null ? fileNames.length : 0;

			// Connect to User DTP.
			//
			// dataSocket = new Socket(dataHost, dataPort);
			dataSocket = (Socket) getClientSocket.invoke( dataHost, dataPort );
			Representation representation = Representation.ASCII;
			PrintWriter writer = new PrintWriter(representation.getOutputStream(dataSocket));

			// Send long file list.
			//
			serverPI.reply(150, "Opening " + representation.getName() + " mode data connection.");

			// Print the total number of files.
			//
			writer.print("total " + numFiles + "\n");

			// Loop through each file and print its name, size,
			// modification date etc.
			//
			for (int i = 0; i < numFiles; i++){
				String fileName = fileNames[i];
				File file = new File(dir, fileName);
				listFile(file, writer);
			}

			writer.flush();
			reply = serverPI.reply(226, "Transfer complete.");
		} catch (ConnectException e) {
			throw new CommandException(425, "Can't open data connection.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommandException(550, "No such directory.");
		} finally {
			try{
				if (dataSocket != null)
					dataSocket.close();
			} catch (IOException e){
			} catch ( Exception e ){
			}
		}
		return reply;
	}
	*/

	/**
	 * Lists a single file in long format (including file sizes and
	 * modification dates etc.).
	 *
	 * @param file the file to list.
	 * @param writer the writer to print to.
	 */
	private void listFile( FileTreeNode file, PrintWriter writer){
		Date date = new Date(file.lastModified());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd hh:mm");
		String dateStr = dateFormat.format(date);

		long size = file.length();
		String sizeStr = Long.toString(size);
		int sizePadLength = Math.max(8 - sizeStr.length(), 0);
		String sizeField = pad(sizePadLength) + sizeStr;

		writer.print(file.isDirectory() ? 'd' : '-');
		writer.print("r-xr-xr-x");
		writer.print(" ");
		writer.print("  1");
		writer.print(" ");
		writer.print("ftp     ");
		writer.print(" ");
		writer.print("ftp     ");
		writer.print(" ");
		writer.print(sizeField);
		writer.print(" ");
		writer.print(dateStr);
		writer.print(" ");
		writer.print(file.getName());

		writer.print('\n');
	}

	private static String pad(int length){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++)
			buf.append((char)' ');
		return buf.toString();
	}
}
