package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class PwdHandler implements CommandHandler {

	private Lambda0 getPwd;
	private Lambda2 reply;

	public PwdHandler( Lambda2 reply, Lambda0 getPwd ){
		this.getPwd = getPwd;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			reply.invoke( new Integer( 257 ), "\"" + getPwd.invoke().toString() + "\"" );
		} catch ( Exception e ){
		}
	}
}
