package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda0;
import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class CwdHandler implements CommandHandler {

	private Lambda1 setDirectory;
	private Lambda2 reply;

	public CwdHandler( Lambda2 reply, Lambda1 setDirectory ){
		this.reply = reply;
		this.setDirectory = setDirectory;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		/*
		String arg = st.nextToken();

		String newDir = arg;
		if (newDir.length() == 0)
			newDir = "/";

		newDir = resolvePath(newDir);

		File file = new File(createNativePath(newDir));
		if (!file.exists())
			throw new CommandException(550, arg + ": no such directory");
		if (!file.isDirectory())
			throw new CommandException(550, arg + ": not a directory");

		currentDir = newDir;
		Logger.log(Logger.LOG_DEBUG, "new cwd = " + currentDir);
		*/
		String arg = "";
		while ( st.hasMoreTokens() ){
			arg = arg + st.nextToken() + " ";
		}
		/*
		if ( st.hasMoreTokens() ){
			arg = st.nextToken();
		}
		if ( ! arg.startsWith( "/" ) ){
			arg = "/" + arg;
		}
		*/
		arg = arg.trim().replaceAll( "~", "" );
		try{
			Boolean b = (Boolean) setDirectory.invoke( arg );
			if ( b.booleanValue() ){
				reply.invoke( new Integer( 200 ), "directory changed to " + arg );
			} else {
				reply.invoke( new Integer( 550 ), "invalid directory" );
			}
		} catch ( Exception e ){
		}
	}
}
