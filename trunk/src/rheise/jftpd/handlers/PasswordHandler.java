package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class PasswordHandler implements CommandHandler {

	private Lambda0 getUserName;
	private Lambda1 setPassword;
	private Lambda2 reply;

	public PasswordHandler( Lambda2 reply, Lambda0 getUserName, Lambda1 setPassword ){
		this.reply = reply;
		this.setPassword = setPassword;
		this.getUserName = getUserName;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {

		try{
			if ( getUserName.invoke() == null){
				throw new CommandException( 503, "Login with USER first.");
			}

			String password = null;
			if (st.hasMoreTokens()){
				password = st.nextToken();
			} else {
				password = "";
			}

			/*
			 * Support for rheise.os when it implements authentication:
			 *
			 * http://www.progsoc.uts.edu.au/~rheise/projects/rheise.os/
			 *
			 try
			 {
			 PasswordCredentials credentials = new PasswordCredentials(username, password);
			 User user = UserManager.getUser(username);
			 UserToken userToken = SecuritySystem.assumeUser(user, credentials);
			 SecuritySystem.setEffectiveUserToken(userToken);
			 }
			 catch (AuthenticationException e)
			 {
			// XXX: keep track of repeated failures.
			throw new CommandException(530, "Login incorrect.");
			}
			*/
			setPassword.invoke( password );
			reply.invoke( new Integer( 230 ), "User " + getUserName.invoke() + " logged in." );
		} catch ( Exception e ){
		}
	}
}
