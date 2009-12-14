package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import java.io.IOException;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class PasvHandler implements CommandHandler {

	private Lambda1 setPassivePort;
	private Lambda2 reply;
	private SocketAddress client;

	public PasvHandler( Lambda2 reply, SocketAddress client, Lambda1 setPassivePort ){
		this.setPassivePort = setPassivePort;
		this.reply = reply;
		this.client = client;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {

		try{
			final ServerSocket server = new ServerSocket( 0 );
			int port = server.getLocalPort();

			int high = port >> 8;
			int low = port & 0xff;

			System.out.println( "Binding pasv port to " + port );
			// System.out.println( "Socket address = " + ((InetSocketAddress) this.client).getAddress().toString() );

			InetAddress address = ((InetSocketAddress) this.client).getAddress();
			/* why does byte & 0xff coerce it into an
			 * unsigned int? i dont know, and i dont care
			 */
			int a1 = address.getAddress()[ 0 ] & 0xff;
			int a2 = address.getAddress()[ 1 ] & 0xff;
			int a3 = address.getAddress()[ 2 ] & 0xff;
			int a4 = address.getAddress()[ 3 ] & 0xff;

			Lambda0 set = new Lambda0(){
				public Object invoke() throws Exception {
					Socket ret = server.accept();
					server.close();
					return ret;
				}
			};

			// reply.invoke( new Integer( 227 ), "Entering Passive Mode (0,0,0,0," + String.valueOf( high ) + "," + String.valueOf( low ) + ")" );
			// System.out.println( "Replying with Entering Passive Mode (" + a1 + "," + a2 + "," + a3 + "," + a4 + "," + String.valueOf( high ) + "," + String.valueOf( low ) + ")" );
			reply.invoke( new Integer( 227 ), "Entering Passive Mode (" + a1 + "," + a2 + "," + a3 + "," + a4 + "," + String.valueOf( high ) + "," + String.valueOf( low ) + ")" );
			setPassivePort.invoke( set );
		} catch ( IOException ie ){
			throw new CommandException( 500, "Could not open PASV", ie );
		} catch ( Exception ce ){
			throw new CommandException( 500, "Could not open PASV", ce );
		}

			/*
		try{
			setPassivePort.invoke();
			int port = ((Integer) setPassivePort.invoke()).intValue();

			int high = port >> 8;
			int low = port & 0xff;

			reply.invoke( new Integer( 227 ), "Entering Passive Mode (0,0,0,0," + String.valueOf( high ) + "," + String.valueOf( low ) + ")" );
		} catch ( Exception e ){
		}
			*/
	}
}
