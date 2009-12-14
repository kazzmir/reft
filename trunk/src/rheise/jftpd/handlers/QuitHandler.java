package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class QuitHandler implements CommandHandler {

	private Lambda0 quit;
	private Lambda2 reply;

	public QuitHandler( Lambda2 reply, Lambda0 quit ){
		this.quit = quit;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			quit.invoke();
			reply.invoke( new Integer( 221 ), "Later!" );
		} catch ( Exception e ){
		}
	}
}
