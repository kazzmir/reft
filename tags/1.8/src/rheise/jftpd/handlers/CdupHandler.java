package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class CdupHandler implements CommandHandler {

	private Lambda2 reply;
	private Lambda0 getCurrentDir;
	private Lambda1 setCurrentDir;

	public CdupHandler( Lambda2 reply, Lambda0 getCurrentDir, Lambda1 setCurrentDir ){
		this.reply = reply;
		this.getCurrentDir = getCurrentDir;
		this.setCurrentDir = setCurrentDir;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String dir = (String) getCurrentDir.invoke();

			while ( dir.endsWith( "/" ) ){
				dir = dir.substring( 0, dir.length() - 1 );
			}
			if ( dir.lastIndexOf( "/" ) != -1 ){
				dir = dir.substring( 0, dir.lastIndexOf( "/" ) );
			}
			dir = "/" + dir;
			Boolean b = (Boolean) setCurrentDir.invoke( dir );
			if ( b.booleanValue() ){
				reply.invoke( new Integer( 250 ), "CDUP command successful." );
			} else {
				reply.invoke( new Integer( 550 ), "CDUP failed" );
			}
		} catch ( Exception e ){
		}
	}
}
