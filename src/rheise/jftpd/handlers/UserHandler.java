package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class UserHandler implements CommandHandler {

	private Lambda1 setUserName;
	private Lambda2 reply;

	public UserHandler( Lambda2 reply, Lambda1 setUserName ){
		this.setUserName = setUserName;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String username = st.nextToken();
			setUserName.invoke( username );
			reply.invoke( new Integer( 331 ), "Password required for " + username + "." );
		} catch ( Exception e ){
		}
	}
}
