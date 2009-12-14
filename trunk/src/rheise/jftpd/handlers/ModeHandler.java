package rheise.jftpd.handlers;

import com.rafkind.reft.Lambda1;
import com.rafkind.reft.Lambda2;

import rheise.jftpd.TransmissionMode;

import rheise.jftpd.CommandHandler;
import rheise.jftpd.CommandException;

import java.util.StringTokenizer;

public class ModeHandler implements CommandHandler {

	private Lambda1 setTransmissionMode;
	private Lambda2 reply;

	public ModeHandler( Lambda2 reply, Lambda1 setTransmissionMode ){
		this.setTransmissionMode = setTransmissionMode;
		this.reply = reply;
	}

	public void handle( String line, StringTokenizer st ) throws CommandException {
		try{
			String arg = st.nextToken().toUpperCase();
			if (arg.length() != 1){
				throw new CommandException(500, "MODE: invalid argument '" + arg + "'");
			}

			char code = arg.charAt(0);
			TransmissionMode mode = TransmissionMode.get( code );
			if ( mode == null ){
				throw new CommandException(500, "MODE: invalid argument '" + arg + "'");
			}
			setTransmissionMode.invoke( mode );
			reply.invoke( new Integer( 200 ), "MODE " + arg + " ok." );
		} catch ( Exception ex ){
		}
	}
}
