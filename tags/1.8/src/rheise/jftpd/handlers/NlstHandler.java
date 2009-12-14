package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class NlstHandler implements CommandHandler {

	private Lambda0 getCurrentDirectory;
	private Lambda1 sendList;
	private Lambda2 reply;

	public NlstHandler( Lambda2 reply, Lambda1 sendList, Lambda0 getCurrentDirectory ){
		this.sendList = sendList;
		this.getCurrentDirectory = getCurrentDirectory;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String path = null;
			if (st.hasMoreTokens()){
				path = st.nextToken();
			} else {
				path = (String) getCurrentDirectory.invoke();
			}

			sendList.invoke( path );
		} catch ( CommandException ce ){
			throw ce;
		} catch ( Exception e ){
			throw new CommandException( 550, "No such directory", e );
		}
	}
}
