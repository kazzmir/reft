package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

/* restart session */
public class ReinHandler implements CommandHandler {

	private Lambda2 reply;
	private Lambda0 reset;

	public ReinHandler( Lambda2 reply, Lambda0 reset ){
		this.reply = reply;
		this.reset = reset;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			reset.invoke();
			reply.invoke( new Integer( 220 ), "Service ready for new user" );
		} catch ( CommandException ce ){
			throw ce;
		} catch ( Exception e ){
		}
	}
}
