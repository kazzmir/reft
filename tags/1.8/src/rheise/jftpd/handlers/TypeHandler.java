package rheise.jftpd.handlers;

import rheise.jftpd.Representation;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class TypeHandler implements CommandHandler {

	private Lambda1 setRepresentation;
	private Lambda2 reply;

	public TypeHandler( Lambda2 reply, Lambda1 setRepresentation ){
		this.setRepresentation = setRepresentation;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String arg = st.nextToken().toUpperCase();
			if (arg.length() != 1)
				throw new CommandException(500, "TYPE: invalid argument '" + arg + "'");
			char code = arg.charAt(0);
			Representation representation = Representation.get(code);
			if (representation == null)
				throw new CommandException(500, "TYPE: invalid argument '" + arg + "'");
			setRepresentation.invoke( representation );
			reply.invoke( new Integer( 200 ), "Type set to " + arg );
		} catch ( Exception e ){
		}
	}
}
