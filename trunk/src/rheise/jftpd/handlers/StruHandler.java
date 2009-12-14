
package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class StruHandler implements CommandHandler {

	private Lambda1 setDataStructure;
	private Lambda2 reply;

	public StruHandler( Lambda2 reply, Lambda1 setDataStructure ){
		this.setDataStructure = setDataStructure;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String arg = st.nextToken().toUpperCase();
			if ( arg.length() != 1 ){
				throw new CommandException( 500, "STRU: argument must be of length 1" );
			}
			char stru = arg.charAt(0);
			switch (stru){
				case 'F' : {
						   setDataStructure.invoke( new Character( stru ) );
						   break;
					   }
				default:
					   throw new CommandException( 500, "STRU: invalid type '" + arg + "'" );
			}

			reply.invoke( new Integer( 200 ), "STRU " + arg + " ok." );
		} catch ( Exception e ){
		}
	}
}
