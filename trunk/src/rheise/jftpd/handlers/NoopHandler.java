package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class NoopHandler implements CommandHandler {

	private Lambda2 reply;

	public NoopHandler( Lambda2 reply ){
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			reply.invoke( new Integer( 200 ), "OK" );
		} catch ( Exception e ){
		}
	}
}
